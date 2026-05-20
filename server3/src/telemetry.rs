use std::{
    collections::{HashMap, VecDeque},
    sync::Arc,
    time::Instant,
};

use chrono::{Duration as ChronoDuration, NaiveDate, Utc};
use serde::{Deserialize, Serialize};
use tokio::sync::RwLock;

use crate::config::AppConfig;

const MAI_UPSTREAM_URL: &str = "https://public.mai.ru/schedule/data/groups.json";
const EXLER_UPSTREAM_URL: &str = "https://mai-sten.online/prepods/";

#[derive(Clone)]
pub struct Telemetry {
    started_at: chrono::DateTime<Utc>,
    started_instant: Instant,
    retention_days: i64,
    inner: Arc<RwLock<TelemetryStore>>,
}

#[derive(Clone, Default, Debug, Deserialize, Serialize)]
struct TelemetryStore {
    days: Vec<DailyStats>,
    upstreams: HashMap<String, VecDeque<UpstreamProbe>>,
    cache_workers: CacheWorkerStore,
}

#[derive(Clone, Default, Debug, Deserialize, Serialize)]
struct DailyStats {
    day: NaiveDate,
    total_requests: u64,
    endpoint_requests: HashMap<String, u64>,
    popular_schedules: HashMap<String, u64>,
    popular_groups: HashMap<String, u64>,
    popular_exler_reviews: HashMap<String, u64>,
    cache_worker_errors: HashMap<String, u64>,
    cache_worker_timeouts: HashMap<String, u64>,
    cache_waiters: HashMap<String, u64>,
    max_cache_workers_active: u64,
    max_cache_workers_queued: u64,
}

#[derive(Clone, Debug, Deserialize, Serialize)]
pub struct UpstreamProbe {
    pub checked_at: i64,
    pub ok: bool,
    pub status: Option<u16>,
    pub latency_ms: u128,
    pub error: Option<String>,
}

#[derive(Clone, Debug, Serialize)]
pub struct TelemetrySnapshot {
    pub started_at: String,
    pub uptime_seconds: u64,
    pub retention_days: i64,
    pub total_requests: u64,
    pub endpoint_requests: Vec<CountEntry>,
    pub popular_schedules: Vec<CountEntry>,
    pub popular_groups: Vec<CountEntry>,
    pub popular_exler_reviews: Vec<CountEntry>,
    pub upstreams: Vec<UpstreamSnapshot>,
    pub cache_workers: CacheWorkerSnapshot,
}

#[derive(Clone, Debug, Serialize)]
pub struct CountEntry {
    pub key: String,
    pub count: u64,
}

#[derive(Clone, Debug, Serialize)]
pub struct UpstreamSnapshot {
    pub name: String,
    pub current_ok: Option<bool>,
    pub current_status: Option<u16>,
    pub current_latency_ms: Option<u128>,
    pub current_checked_at: Option<i64>,
    pub uptime_percent_month: f64,
    pub probes_month: usize,
    pub timeline: Vec<UpstreamProbe>,
}

#[derive(Clone, Default, Debug, Deserialize, Serialize)]
struct CacheWorkerStore {
    #[serde(skip)]
    active: u64,
    #[serde(skip)]
    queued: u64,
    #[serde(skip)]
    active_workers: HashMap<String, ActiveCacheWorker>,
    queue_timeline: VecDeque<CacheQueueSample>,
    recent_failures: VecDeque<CacheWorkerFailure>,
}

#[derive(Clone, Debug, Deserialize, Serialize)]
pub struct ActiveCacheWorker {
    pub id: String,
    pub namespace: String,
    pub key: String,
    pub started_at: i64,
    pub running_ms: u128,
    pub wait_ms: u128,
}

#[derive(Clone, Debug, Deserialize, Serialize)]
pub struct CacheQueueSample {
    pub at: i64,
    pub namespace: String,
    pub active: u64,
    pub queued: u64,
    pub wait_ms: Option<u128>,
}

#[derive(Clone, Debug, Deserialize, Serialize)]
pub struct CacheWorkerFailure {
    pub at: i64,
    pub namespace: String,
    pub kind: String,
    pub message: String,
}

#[derive(Clone, Debug, Serialize)]
pub struct CacheWorkerSnapshot {
    pub active: u64,
    pub queued: u64,
    pub max_active_month: u64,
    pub max_queued_month: u64,
    pub active_workers: Vec<ActiveCacheWorker>,
    pub waiters: Vec<CountEntry>,
    pub errors: Vec<CountEntry>,
    pub timeouts: Vec<CountEntry>,
    pub queue_timeline: Vec<CacheQueueSample>,
    pub recent_failures: Vec<CacheWorkerFailure>,
}

impl Telemetry {
    pub async fn load_or_new(config: &AppConfig) -> Self {
        let store = match tokio::fs::read_to_string(&config.telemetry_path).await {
            Ok(text) => serde_json::from_str::<TelemetryStore>(&text).unwrap_or_else(|error| {
                tracing::warn!("failed to parse telemetry file: {error}");
                TelemetryStore::default()
            }),
            Err(error) if error.kind() == std::io::ErrorKind::NotFound => TelemetryStore::default(),
            Err(error) => {
                tracing::warn!("failed to read telemetry file: {error}");
                TelemetryStore::default()
            }
        };
        let telemetry = Self {
            started_at: Utc::now(),
            started_instant: Instant::now(),
            retention_days: config.telemetry_retention_days,
            inner: Arc::new(RwLock::new(store)),
        };
        telemetry.prune().await;
        telemetry
    }

    pub async fn record_endpoint(&self, endpoint: &'static str) {
        let mut store = self.inner.write().await;
        prune_store(&mut store, self.retention_days);
        let day = today_mut(&mut store);
        day.total_requests += 1;
        *day.endpoint_requests
            .entry(endpoint.to_owned())
            .or_default() += 1;
    }

    pub async fn record_schedule_request(&self, id: &str) {
        let mut store = self.inner.write().await;
        prune_store(&mut store, self.retention_days);
        let day = today_mut(&mut store);
        day.total_requests += 1;
        *day.endpoint_requests
            .entry("/schedule/{id}".to_owned())
            .or_default() += 1;
        *day.popular_schedules.entry(id.to_owned()).or_default() += 1;
        if looks_like_group(id) {
            *day.popular_groups.entry(id.to_owned()).or_default() += 1;
        }
    }

    pub async fn record_exler_review_request(&self, teacher: &str) {
        let mut store = self.inner.write().await;
        prune_store(&mut store, self.retention_days);
        let day = today_mut(&mut store);
        day.total_requests += 1;
        *day.endpoint_requests
            .entry("/exler-teacher/{id}".to_owned())
            .or_default() += 1;
        *day.popular_exler_reviews
            .entry(teacher.to_owned())
            .or_default() += 1;
    }

    pub async fn snapshot(&self) -> TelemetrySnapshot {
        self.prune().await;
        let store = self.inner.read().await;
        let mut endpoint_requests = HashMap::new();
        let mut popular_schedules = HashMap::new();
        let mut popular_groups = HashMap::new();
        let mut popular_exler_reviews = HashMap::new();
        let mut cache_worker_errors = HashMap::new();
        let mut cache_worker_timeouts = HashMap::new();
        let mut cache_waiters = HashMap::new();
        let mut total_requests = 0;
        let mut max_active_month = 0u64;
        let mut max_queued_month = 0u64;

        for day in &store.days {
            total_requests += day.total_requests;
            merge_counts(&mut endpoint_requests, &day.endpoint_requests);
            merge_counts(&mut popular_schedules, &day.popular_schedules);
            merge_counts(&mut popular_groups, &day.popular_groups);
            merge_counts(&mut popular_exler_reviews, &day.popular_exler_reviews);
            merge_counts(&mut cache_worker_errors, &day.cache_worker_errors);
            merge_counts(&mut cache_worker_timeouts, &day.cache_worker_timeouts);
            merge_counts(&mut cache_waiters, &day.cache_waiters);
            max_active_month = max_active_month.max(day.max_cache_workers_active);
            max_queued_month = max_queued_month.max(day.max_cache_workers_queued);
        }
        let now = Utc::now().timestamp_millis();
        let mut active_workers = store
            .cache_workers
            .active_workers
            .values()
            .cloned()
            .map(|mut worker| {
                worker.running_ms = now
                    .saturating_sub(worker.started_at.saturating_mul(1000))
                    .max(0) as u128;
                worker
            })
            .collect::<Vec<_>>();
        active_workers.sort_by(|a, b| {
            b.running_ms
                .cmp(&a.running_ms)
                .then_with(|| a.namespace.cmp(&b.namespace))
                .then_with(|| a.key.cmp(&b.key))
        });

        TelemetrySnapshot {
            started_at: self.started_at.to_rfc3339(),
            uptime_seconds: self.started_instant.elapsed().as_secs(),
            retention_days: self.retention_days,
            total_requests,
            endpoint_requests: top_counts(&endpoint_requests, usize::MAX),
            popular_schedules: top_counts(&popular_schedules, usize::MAX),
            popular_groups: top_counts(&popular_groups, usize::MAX),
            popular_exler_reviews: top_counts(&popular_exler_reviews, usize::MAX),
            upstreams: store
                .upstreams
                .iter()
                .map(|(name, timeline)| upstream_snapshot(name, timeline))
                .collect(),
            cache_workers: CacheWorkerSnapshot {
                active: active_workers.len() as u64,
                queued: store.cache_workers.queued,
                max_active_month,
                max_queued_month,
                active_workers,
                waiters: top_counts(&cache_waiters, usize::MAX),
                errors: top_counts(&cache_worker_errors, usize::MAX),
                timeouts: top_counts(&cache_worker_timeouts, usize::MAX),
                queue_timeline: store.cache_workers.queue_timeline.iter().cloned().collect(),
                recent_failures: store
                    .cache_workers
                    .recent_failures
                    .iter()
                    .cloned()
                    .collect(),
            },
        }
    }

    pub async fn record_cache_waiter(&self, namespace: &'static str) {
        let mut store = self.inner.write().await;
        prune_store(&mut store, self.retention_days);
        *today_mut(&mut store)
            .cache_waiters
            .entry(namespace.to_owned())
            .or_default() += 1;
    }

    pub async fn record_cache_worker_queued(&self, namespace: &'static str) {
        let mut store = self.inner.write().await;
        prune_store(&mut store, self.retention_days);
        store.cache_workers.queued += 1;
        let queued = store.cache_workers.queued;
        let day = today_mut(&mut store);
        day.max_cache_workers_queued = day.max_cache_workers_queued.max(queued);
        push_queue_sample(&mut store, namespace, None);
    }

    pub async fn record_cache_worker_started(
        &self,
        namespace: &'static str,
        key: &str,
        wait: std::time::Duration,
    ) {
        let mut store = self.inner.write().await;
        prune_store(&mut store, self.retention_days);
        store.cache_workers.queued = store.cache_workers.queued.saturating_sub(1);
        store.cache_workers.active += 1;
        let active = store.cache_workers.active;
        let id = cache_worker_id(namespace, key);
        store.cache_workers.active_workers.insert(
            id.clone(),
            ActiveCacheWorker {
                id,
                namespace: namespace.to_owned(),
                key: key.to_owned(),
                started_at: Utc::now().timestamp(),
                running_ms: 0,
                wait_ms: wait.as_millis(),
            },
        );
        let day = today_mut(&mut store);
        day.max_cache_workers_active = day.max_cache_workers_active.max(active);
        push_queue_sample(&mut store, namespace, Some(wait.as_millis()));
    }

    pub async fn record_cache_worker_finished(&self, namespace: &'static str, key: &str) {
        let mut store = self.inner.write().await;
        store.cache_workers.active = store.cache_workers.active.saturating_sub(1);
        store
            .cache_workers
            .active_workers
            .remove(&cache_worker_id(namespace, key));
        push_queue_sample(&mut store, namespace, None);
    }

    pub async fn record_cache_worker_failed(
        &self,
        namespace: &'static str,
        key: &str,
        message: String,
    ) {
        let mut store = self.inner.write().await;
        store.cache_workers.active = store.cache_workers.active.saturating_sub(1);
        store
            .cache_workers
            .active_workers
            .remove(&cache_worker_id(namespace, key));
        *today_mut(&mut store)
            .cache_worker_errors
            .entry(namespace.to_owned())
            .or_default() += 1;
        push_failure(&mut store, namespace, "error", message);
        push_queue_sample(&mut store, namespace, None);
    }

    pub async fn record_cache_worker_timeout(
        &self,
        namespace: &'static str,
        key: &str,
        message: String,
    ) {
        let mut store = self.inner.write().await;
        store.cache_workers.active = store.cache_workers.active.saturating_sub(1);
        store
            .cache_workers
            .active_workers
            .remove(&cache_worker_id(namespace, key));
        *today_mut(&mut store)
            .cache_worker_timeouts
            .entry(namespace.to_owned())
            .or_default() += 1;
        push_failure(&mut store, namespace, "timeout", message);
        push_queue_sample(&mut store, namespace, None);
    }

    pub fn spawn_upstream_monitor(self: Arc<Self>, config: AppConfig) {
        tokio::spawn(async move {
            let client = match config.http_client() {
                Ok(client) => client,
                Err(error) => {
                    tracing::error!("failed to create upstream monitor client: {error}");
                    return;
                }
            };
            let mut interval = tokio::time::interval(config.upstream_probe_interval);
            loop {
                interval.tick().await;
                let (mai, exler) = tokio::join!(
                    probe_upstream(&client, MAI_UPSTREAM_URL),
                    probe_upstream(&client, EXLER_UPSTREAM_URL),
                );
                self.record_upstream_probe("mai", mai).await;
                self.record_upstream_probe("exler", exler).await;
            }
        });
    }

    pub fn spawn_persistence(self: Arc<Self>, config: AppConfig) {
        tokio::spawn(async move {
            let mut interval = tokio::time::interval(config.telemetry_flush_interval);
            loop {
                interval.tick().await;
                if let Err(error) = self.save(&config).await {
                    tracing::warn!("failed to save telemetry: {error}");
                }
            }
        });
    }

    async fn record_upstream_probe(&self, name: &'static str, probe: UpstreamProbe) {
        let mut store = self.inner.write().await;
        prune_store(&mut store, self.retention_days);
        store
            .upstreams
            .entry(name.to_owned())
            .or_default()
            .push_back(probe);
        prune_store(&mut store, self.retention_days);
    }

    async fn prune(&self) {
        let mut store = self.inner.write().await;
        prune_store(&mut store, self.retention_days);
    }

    async fn save(&self, config: &AppConfig) -> anyhow::Result<()> {
        self.prune().await;
        let store = self.inner.read().await.clone();
        let text = serde_json::to_string_pretty(&store)?;
        if let Some(parent) = config.telemetry_path.parent() {
            tokio::fs::create_dir_all(parent).await?;
        }
        let tmp_path = config.telemetry_path.with_extension("json.tmp");
        tokio::fs::write(&tmp_path, text).await?;
        tokio::fs::rename(tmp_path, &config.telemetry_path).await?;
        Ok(())
    }
}

async fn probe_upstream(client: &reqwest::Client, url: &str) -> UpstreamProbe {
    let started = Instant::now();
    let checked_at = Utc::now().timestamp();
    match client.get(url).send().await {
        Ok(response) => {
            let status = response.status();
            UpstreamProbe {
                checked_at,
                ok: status.is_success(),
                status: Some(status.as_u16()),
                latency_ms: started.elapsed().as_millis(),
                error: None,
            }
        }
        Err(error) => UpstreamProbe {
            checked_at,
            ok: false,
            status: None,
            latency_ms: started.elapsed().as_millis(),
            error: Some(error.to_string()),
        },
    }
}

fn today_mut(store: &mut TelemetryStore) -> &mut DailyStats {
    let today = Utc::now().date_naive();
    if let Some(index) = store.days.iter().position(|day| day.day == today) {
        return &mut store.days[index];
    }
    store.days.push(DailyStats {
        day: today,
        ..Default::default()
    });
    store.days.last_mut().expect("just pushed day stats")
}

fn prune_store(store: &mut TelemetryStore, retention_days: i64) {
    let oldest_day =
        Utc::now().date_naive() - ChronoDuration::days(retention_days.saturating_sub(1));
    store.days.retain(|day| day.day >= oldest_day);

    let oldest_probe = Utc::now() - ChronoDuration::days(retention_days);
    let oldest_probe_ts = oldest_probe.timestamp();
    for probes in store.upstreams.values_mut() {
        while probes
            .front()
            .is_some_and(|probe| probe.checked_at < oldest_probe_ts)
        {
            probes.pop_front();
        }
    }
    while store
        .cache_workers
        .queue_timeline
        .front()
        .is_some_and(|sample| sample.at < oldest_probe_ts)
    {
        store.cache_workers.queue_timeline.pop_front();
    }
    while store
        .cache_workers
        .recent_failures
        .front()
        .is_some_and(|failure| failure.at < oldest_probe_ts)
    {
        store.cache_workers.recent_failures.pop_front();
    }
}

fn upstream_snapshot(name: &str, timeline: &VecDeque<UpstreamProbe>) -> UpstreamSnapshot {
    let ok_count = timeline.iter().filter(|probe| probe.ok).count();
    let uptime_percent_month = if timeline.is_empty() {
        0.0
    } else {
        ok_count as f64 * 100.0 / timeline.len() as f64
    };
    let latest = timeline.back();
    UpstreamSnapshot {
        name: name.to_owned(),
        current_ok: latest.map(|probe| probe.ok),
        current_status: latest.and_then(|probe| probe.status),
        current_latency_ms: latest.map(|probe| probe.latency_ms),
        current_checked_at: latest.map(|probe| probe.checked_at),
        uptime_percent_month,
        probes_month: timeline.len(),
        timeline: timeline.iter().cloned().collect(),
    }
}

fn merge_counts(target: &mut HashMap<String, u64>, source: &HashMap<String, u64>) {
    for (key, count) in source {
        *target.entry(key.clone()).or_default() += count;
    }
}

fn push_queue_sample(store: &mut TelemetryStore, namespace: &'static str, wait_ms: Option<u128>) {
    store
        .cache_workers
        .queue_timeline
        .push_back(CacheQueueSample {
            at: Utc::now().timestamp(),
            namespace: namespace.to_owned(),
            active: store.cache_workers.active,
            queued: store.cache_workers.queued,
            wait_ms,
        });
    while store.cache_workers.queue_timeline.len() > 10_000 {
        store.cache_workers.queue_timeline.pop_front();
    }
}

fn push_failure(
    store: &mut TelemetryStore,
    namespace: &'static str,
    kind: &'static str,
    message: String,
) {
    store
        .cache_workers
        .recent_failures
        .push_back(CacheWorkerFailure {
            at: Utc::now().timestamp(),
            namespace: namespace.to_owned(),
            kind: kind.to_owned(),
            message,
        });
    while store.cache_workers.recent_failures.len() > 200 {
        store.cache_workers.recent_failures.pop_front();
    }
}

fn cache_worker_id(namespace: &'static str, key: &str) -> String {
    format!("{namespace}:{key}")
}

fn top_counts(counts: &HashMap<String, u64>, limit: usize) -> Vec<CountEntry> {
    let mut entries = counts
        .iter()
        .map(|(key, count)| CountEntry {
            key: key.clone(),
            count: *count,
        })
        .collect::<Vec<_>>();
    entries.sort_by(|a, b| b.count.cmp(&a.count).then_with(|| a.key.cmp(&b.key)));
    entries.truncate(limit);
    entries
}

fn looks_like_group(value: &str) -> bool {
    value.contains('-') && value.chars().any(|char_| char_.is_ascii_digit())
}
