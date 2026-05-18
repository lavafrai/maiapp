use std::{
    collections::HashMap,
    future::Future,
    path::PathBuf,
    sync::Arc,
    time::{Duration, Instant},
};

use anyhow::anyhow;
use chrono::{DateTime, Utc};
use futures::{
    FutureExt,
    future::{BoxFuture, Shared},
};
use serde::{Serialize, de::DeserializeOwned};
use tokio::sync::{Mutex, RwLock, Semaphore};

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
    disk_path: PathBuf,
}

impl<T> AsyncCache<T>
where
    T: Clone + Send + Sync + Serialize + DeserializeOwned + 'static,
{
    pub fn new(namespace: &'static str, config: &AppConfig, telemetry: Arc<Telemetry>) -> Self {
        let disk_path = config.cache_dir.join(format!("{namespace}.json"));
        let values = load_cache_values(&disk_path).unwrap_or_else(|error| {
            tracing::warn!("failed to load {namespace} cache from disk: {error}");
            HashMap::new()
        });
        Self {
            namespace,
            fresh_ttl: config.cache_fresh_ttl,
            valid_ttl: config.cache_valid_ttl.max(config.cache_fresh_ttl),
            worker_timeout: config.cache_worker_timeout,
            values: Arc::new(RwLock::new(values)),
            inflight: Arc::new(Mutex::new(HashMap::new())),
            workers: Arc::new(Semaphore::new(config.cache_max_workers.max(1))),
            telemetry,
            disk_path,
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
        let future = self.make_worker(key.clone(), fetch).shared();
        inflight.insert(key.clone(), future.clone());
        drop(inflight);

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
                let future = self.make_worker(key.clone(), fetch).shared();
                inflight.insert(key.clone(), future.clone());
                future
            }
        };

        future.await.map_err(|message| anyhow!(message))
    }

    fn make_worker<F, Fut>(&self, key: String, fetch: F) -> BoxFuture<'static, Result<T, String>>
    where
        F: FnOnce() -> Fut + Send + 'static,
        Fut: Future<Output = anyhow::Result<T>> + Send + 'static,
    {
        let namespace = self.namespace;
        let timeout = self.worker_timeout;
        let workers = self.workers.clone();
        let values = self.values.clone();
        let inflight = self.inflight.clone();
        let telemetry = self.telemetry.clone();
        let disk_path = self.disk_path.clone();

        async move {
            telemetry.record_cache_worker_queued(namespace).await;
            let queued_at = Instant::now();
            let permit = workers
                .acquire_owned()
                .await
                .map_err(|error| error.to_string())?;
            telemetry
                .record_cache_worker_started(namespace, queued_at.elapsed())
                .await;

            let result = tokio::time::timeout(timeout, fetch()).await;
            drop(permit);

            let output = match result {
                Ok(Ok(value)) => {
                    let snapshot = {
                        let mut values = values.write().await;
                        values.insert(key.clone(), CacheEntry::new(value.clone()));
                        values.clone()
                    };
                    if let Err(error) = persist_cache_values(&disk_path, &snapshot).await {
                        tracing::warn!("failed to persist {namespace} cache: {error}");
                    }
                    telemetry.record_cache_worker_finished(namespace).await;
                    Ok(value)
                }
                Ok(Err(error)) => {
                    telemetry
                        .record_cache_worker_failed(namespace, error.to_string())
                        .await;
                    Err(error.to_string())
                }
                Err(_) => {
                    let message = format!("cache worker timed out after {}s", timeout.as_secs());
                    telemetry
                        .record_cache_worker_timeout(namespace, message.clone())
                        .await;
                    Err(message)
                }
            };

            inflight.lock().await.remove(&key);
            output
        }
        .boxed()
    }
}

fn load_cache_values<T>(path: &PathBuf) -> anyhow::Result<HashMap<String, CacheEntry<T>>>
where
    T: DeserializeOwned,
{
    match std::fs::read_to_string(path) {
        Ok(text) => Ok(serde_json::from_str(&text)?),
        Err(error) if error.kind() == std::io::ErrorKind::NotFound => Ok(HashMap::new()),
        Err(error) => Err(error.into()),
    }
}

async fn persist_cache_values<T>(
    path: &PathBuf,
    values: &HashMap<String, CacheEntry<T>>,
) -> anyhow::Result<()>
where
    T: Serialize,
{
    if let Some(parent) = path.parent() {
        tokio::fs::create_dir_all(parent).await?;
    }
    let text = serde_json::to_string_pretty(values)?;
    let tmp_path = path.with_extension("json.tmp");
    tokio::fs::write(&tmp_path, text).await?;
    tokio::fs::rename(tmp_path, path).await?;
    Ok(())
}
