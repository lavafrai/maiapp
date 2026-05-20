use std::{collections::HashMap, future::Future, path::PathBuf, sync::Arc, time::Duration};

use anyhow::anyhow;
use chrono::{DateTime, Utc};
use futures::{
    FutureExt,
    future::{BoxFuture, Shared},
};
use serde::{Serialize, de::DeserializeOwned};
use tokio::sync::{Mutex, RwLock, Semaphore, oneshot};

use crate::{config::AppConfig, telemetry::Telemetry};

#[derive(Clone, serde::Deserialize, serde::Serialize)]
pub struct CacheEntry<T> {
    pub value: T,
    pub refreshed_at: DateTime<Utc>,
}

impl<T> CacheEntry<T> {
    pub fn new(value: T) -> Self {
        Self {
            value,
            refreshed_at: Utc::now(),
        }
    }

    pub fn age(&self) -> Duration {
        Utc::now()
            .signed_duration_since(self.refreshed_at)
            .to_std()
            .unwrap_or_default()
    }
}

#[derive(Clone, Copy, Debug, PartialEq, Eq)]
pub enum CacheStatus {
    Hit,
    Stale,
    Miss,
}

impl CacheStatus {
    pub fn as_header(self) -> &'static str {
        match self {
            Self::Hit => "hit",
            Self::Stale => "stale",
            Self::Miss => "miss",
        }
    }
}

#[derive(Clone, Debug)]
pub struct Cached<T> {
    pub value: T,
    pub status: CacheStatus,
}

pub struct AsyncCache<T> {
    namespace: &'static str,
    fresh_ttl: Duration,
    valid_ttl: Duration,
    worker_timeout: Duration,
    values: Arc<RwLock<HashMap<String, CacheEntry<T>>>>,
    inflight: Arc<Mutex<HashMap<String, Shared<BoxFuture<'static, Result<T, String>>>>>>,
    workers: Arc<Semaphore>,
    telemetry: Arc<Telemetry>,
    disk_dir: PathBuf,
}

impl<T> AsyncCache<T>
where
    T: Clone + Send + Sync + Serialize + DeserializeOwned + 'static,
{
    pub fn new(namespace: &'static str, config: &AppConfig, telemetry: Arc<Telemetry>) -> Self {
        let disk_dir = config.cache_dir.join(namespace);
        let legacy_disk_path = config.cache_dir.join(format!("{namespace}.json"));
        let valid_ttl = config.cache_valid_ttl.max(config.cache_fresh_ttl);
        let values =
            load_cache_values(&disk_dir, &legacy_disk_path, valid_ttl).unwrap_or_else(|error| {
                tracing::warn!("failed to load {namespace} cache from disk: {error}");
                HashMap::new()
            });
        Self {
            namespace,
            fresh_ttl: config.cache_fresh_ttl,
            valid_ttl,
            worker_timeout: config.cache_worker_timeout,
            values: Arc::new(RwLock::new(values)),
            inflight: Arc::new(Mutex::new(HashMap::new())),
            workers: Arc::new(Semaphore::new(config.cache_max_workers.max(1))),
            telemetry,
            disk_dir,
        }
    }

    pub async fn get_or_refresh<F, Fut>(&self, key: String, fetch: F) -> anyhow::Result<Cached<T>>
    where
        F: FnOnce() -> Fut + Send + 'static,
        Fut: Future<Output = anyhow::Result<T>> + Send + 'static,
    {
        if let Some(entry) = self.values.read().await.get(&key).cloned() {
            let age = entry.age();
            if age < self.fresh_ttl {
                return Ok(Cached {
                    value: entry.value,
                    status: CacheStatus::Hit,
                });
            }
            if age < self.valid_ttl {
                self.spawn_refresh(key, fetch).await;
                return Ok(Cached {
                    value: entry.value,
                    status: CacheStatus::Stale,
                });
            }
        }

        let value = self.run_or_join(key, fetch, true).await?;
        Ok(Cached {
            value,
            status: CacheStatus::Miss,
        })
    }

    async fn spawn_refresh<F, Fut>(&self, key: String, fetch: F)
    where
        F: FnOnce() -> Fut + Send + 'static,
        Fut: Future<Output = anyhow::Result<T>> + Send + 'static,
    {
        let mut inflight = self.inflight.lock().await;
        if inflight.contains_key(&key) {
            return;
        }
        let (future, start_worker) = self.start_worker(key.clone(), fetch);
        inflight.insert(key, future.clone());
        drop(inflight);
        let _ = start_worker.send(());

        tokio::spawn(async move {
            let _ = future.await;
        });
    }

    async fn run_or_join<F, Fut>(
        &self,
        key: String,
        fetch: F,
        count_waiter: bool,
    ) -> anyhow::Result<T>
    where
        F: FnOnce() -> Fut + Send + 'static,
        Fut: Future<Output = anyhow::Result<T>> + Send + 'static,
    {
        let future = {
            let mut inflight = self.inflight.lock().await;
            if let Some(existing) = inflight.get(&key) {
                if count_waiter {
                    self.telemetry.record_cache_waiter(self.namespace).await;
                }
                existing.clone()
            } else {
                let (future, start_worker) = self.start_worker(key.clone(), fetch);
                inflight.insert(key.clone(), future.clone());
                let _ = start_worker.send(());
                future
            }
        };

        future.await.map_err(|message| anyhow!(message))
    }

    fn start_worker<F, Fut>(
        &self,
        key: String,
        fetch: F,
    ) -> (
        Shared<BoxFuture<'static, Result<T, String>>>,
        oneshot::Sender<()>,
    )
    where
        F: FnOnce() -> Fut + Send + 'static,
        Fut: Future<Output = anyhow::Result<T>> + Send + 'static,
    {
        let namespace = self.namespace;
        let workers = self.workers.clone();
        let timeout = self.worker_timeout;
        let valid_ttl = self.valid_ttl;
        let values = self.values.clone();
        let inflight = self.inflight.clone();
        let telemetry = self.telemetry.clone();
        let disk_dir = self.disk_dir.clone();

        let worker_key = key.clone();
        let (start_worker, wait_until_registered) = oneshot::channel();
        let handle = tokio::spawn(async move {
            let _ = wait_until_registered.await;
            telemetry.record_cache_worker_queued(namespace).await;
            let queued_at = std::time::Instant::now();
            let permit = workers
                .acquire_owned()
                .await
                .map_err(|error| error.to_string())?;
            telemetry
                .record_cache_worker_started(namespace, &worker_key, queued_at.elapsed())
                .await;

            let result = tokio::time::timeout(timeout, fetch()).await;
            drop(permit);

            let output = match result {
                Ok(Ok(value)) => {
                    let entry = CacheEntry::new(value.clone());
                    {
                        let mut values = values.write().await;
                        values.retain(|_, entry| entry.age() < valid_ttl);
                        values.insert(key.clone(), entry.clone());
                    }
                    if let Err(error) = persist_cache_entry(&disk_dir, &key, &entry).await {
                        tracing::warn!("failed to persist {namespace} cache entry {key}: {error}");
                    }
                    telemetry
                        .record_cache_worker_finished(namespace, &worker_key)
                        .await;
                    Ok(value)
                }
                Ok(Err(error)) => {
                    telemetry
                        .record_cache_worker_failed(namespace, &worker_key, error.to_string())
                        .await;
                    Err(error.to_string())
                }
                Err(_) => {
                    let message = format!("cache worker timed out after {}s", timeout.as_secs());
                    telemetry
                        .record_cache_worker_timeout(namespace, &worker_key, message.clone())
                        .await;
                    Err(message)
                }
            };

            inflight.lock().await.remove(&key);
            output
        });

        let future = async move {
            match handle.await {
                Ok(result) => result,
                Err(error) => Err(format!("cache worker task failed: {error}")),
            }
        }
        .boxed()
        .shared();
        (future, start_worker)
    }
}

fn load_cache_values<T>(
    dir: &PathBuf,
    legacy_path: &PathBuf,
    valid_ttl: Duration,
) -> anyhow::Result<HashMap<String, CacheEntry<T>>>
where
    T: DeserializeOwned,
{
    let values = load_cache_dir(dir, valid_ttl)?;
    if !values.is_empty() {
        return Ok(values);
    }

    match std::fs::read_to_string(legacy_path) {
        Ok(text) => {
            let mut values: HashMap<String, CacheEntry<T>> = serde_json::from_str(&text)?;
            values.retain(|_, entry| entry.age() < valid_ttl);
            Ok(values)
        }
        Err(error) if error.kind() == std::io::ErrorKind::NotFound => Ok(HashMap::new()),
        Err(error) => Err(error.into()),
    }
}

fn load_cache_dir<T>(
    dir: &PathBuf,
    valid_ttl: Duration,
) -> anyhow::Result<HashMap<String, CacheEntry<T>>>
where
    T: DeserializeOwned,
{
    let mut values = HashMap::new();
    let entries = match std::fs::read_dir(dir) {
        Ok(entries) => entries,
        Err(error) if error.kind() == std::io::ErrorKind::NotFound => return Ok(values),
        Err(error) => return Err(error.into()),
    };
    for entry in entries {
        let entry = entry?;
        let path = entry.path();
        if path.extension().and_then(|value| value.to_str()) != Some("json") {
            continue;
        }
        let Some(file_stem) = path.file_stem().and_then(|value| value.to_str()) else {
            continue;
        };
        let key = decode_cache_key(file_stem)?;
        let text = std::fs::read_to_string(&path)?;
        let value: CacheEntry<T> = serde_json::from_str(&text)?;
        if value.age() < valid_ttl {
            values.insert(key, value);
        }
    }
    Ok(values)
}

async fn persist_cache_entry<T>(
    dir: &PathBuf,
    key: &str,
    entry: &CacheEntry<T>,
) -> anyhow::Result<()>
where
    T: Serialize,
{
    tokio::fs::create_dir_all(dir).await?;
    let text = serde_json::to_string(entry)?;
    let path = dir.join(format!("{}.json", encode_cache_key(key)));
    let tmp_path = path.with_extension("json.tmp");
    tokio::fs::write(&tmp_path, text).await?;
    tokio::fs::rename(tmp_path, path).await?;
    Ok(())
}

fn encode_cache_key(key: &str) -> String {
    key.as_bytes()
        .iter()
        .map(|byte| format!("{byte:02x}"))
        .collect()
}

fn decode_cache_key(value: &str) -> anyhow::Result<String> {
    if !value.len().is_multiple_of(2) {
        anyhow::bail!("invalid cache key encoding");
    }
    let mut bytes = Vec::with_capacity(value.len() / 2);
    for index in (0..value.len()).step_by(2) {
        bytes.push(u8::from_str_radix(&value[index..index + 2], 16)?);
    }
    Ok(String::from_utf8(bytes)?)
}
