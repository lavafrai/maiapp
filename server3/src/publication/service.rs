use std::{path::PathBuf, sync::Arc, time::{Duration, Instant}};

use anyhow::{Context, ensure};
use chrono::Utc;
use futures::future::BoxFuture;
use tokio::sync::{Mutex, OwnedMutexGuard, OwnedSemaphorePermit, Semaphore};

use super::{
    domain::{SCHEMA_VERSION, Source, digest, normalize, reconcile},
    exporters::{ExportArtifact, ExportFormat, ExportRegistry},
    storage::{PublicationRecord, PublicationStore},
};
use crate::{models::Schedule, repositories::mai::MaiRepository};

/// New sources (e.g. opt-in personal publications) implement this independently
/// of the registry. The caller, not an exporter, owns fetching and validation.
pub trait ScheduleProvider: Send + Sync {
    fn fetch<'a>(&'a self, source: &'a Source) -> BoxFuture<'a, anyhow::Result<Schedule>>;
}

impl ScheduleProvider for MaiRepository {
    fn fetch<'a>(&'a self, source: &'a Source) -> BoxFuture<'a, anyhow::Result<Schedule>> {
        Box::pin(async move { Ok(self.schedule_by_id(&source.id).await?.value) })
    }
}

#[derive(Clone)]
pub struct PublicationConfig {
    pub directory: PathBuf,
    pub refresh_interval: Duration,
    pub failure_backoff: Duration,
    pub worker_timeout: Duration,
    pub confirmation_seconds: i64,
}

impl PublicationConfig {
    pub fn from_env() -> Self {
        Self {
            directory: std::env::var_os("MAIAPP_PUBLICATIONS_DIR").map(PathBuf::from)
                .unwrap_or_else(|| PathBuf::from("/var/lib/maiapp-server3/publications")),
            refresh_interval: Duration::from_secs(15 * 60),
            failure_backoff: Duration::from_secs(60),
            worker_timeout: Duration::from_secs(40),
            confirmation_seconds: 15 * 60,
        }
    }
}

struct Attempt {
    key: String,
    next_allowed: Instant,
}

type RefreshPermit = (OwnedMutexGuard<Attempt>, OwnedSemaphorePermit);

pub struct PublicationOutput {
    pub artifact: ExportArtifact,
    pub stale: bool,
    pub held: bool,
    pub source_observed_at: i64,
}

pub struct PublicationService {
    provider: Arc<dyn ScheduleProvider>,
    pub store: PublicationStore,
    pub registry: ExportRegistry,
    config: PublicationConfig,
    // Fixed-size striping avoids an unbounded mutex map for public identifiers.
    // One writer per stripe; hash collisions only delay refresh, never mix data.
    gates: Vec<Arc<Mutex<Attempt>>>,
    workers: Arc<Semaphore>,
}

impl PublicationService {
    pub fn new(provider: Arc<dyn ScheduleProvider>, config: PublicationConfig) -> Self {
        Self {
            store: PublicationStore::new(config.directory.clone()), provider, config,
            registry: ExportRegistry::default(), workers: Arc::new(Semaphore::new(4)),
            gates: (0..64).map(|_| Arc::new(Mutex::new(Attempt {
                key: String::new(), next_allowed: Instant::now(),
            }))).collect(),
        }
    }

    pub fn formats(&self) -> Vec<ExportFormat> { self.registry.formats() }

    fn permit(&self, source: &Source) -> Option<RefreshPermit> {
        let key = source.key();
        let hash = md5::compute(key.as_bytes());
        let gate = self.gates[usize::from(hash.0[0]) % self.gates.len()].clone();
        let mut attempt = gate.try_lock_owned().ok()?;
        if attempt.key == key && attempt.next_allowed > Instant::now() { return None; }
        let worker = self.workers.clone().try_acquire_owned().ok()?;
        attempt.key = key;
        attempt.next_allowed = Instant::now() + self.config.failure_backoff;
        Some((attempt, worker))
    }

    /// Existing subscribers never wait for an upstream request. Updates run on
    /// demand when calendars poll, with bounded single-flight workers.
    pub async fn get(self: &Arc<Self>, source: Source, format: &str) -> anyhow::Result<PublicationOutput> {
        let exporter = self.registry.get(format).context("unknown export format")?;
        let existing = self.store.load(&source).await?;
        let now = Utc::now().timestamp();
        if let Some(record) = existing {
            let artifact = match record.artifacts.get(format) {
                Some(artifact) => artifact.clone(),
                // A newly registered format can render a persisted snapshot
                // even when its source is offline. Existing formats are isolated.
                None => ExportArtifact::render(exporter, &record.state.snapshot, now)?,
            };
            let stale = record.state.pending.is_some()
                || now.saturating_sub(record.state.source_observed_at) >= self.config.refresh_interval.as_secs() as i64
                || artifact.snapshot_revision != record.state.snapshot.revision;
            if now.saturating_sub(record.state.last_checked_at) >= self.config.refresh_interval.as_secs() as i64 {
                if let Some(permit) = self.permit(&source) {
                    let service = self.clone();
                    let refresh_source = source.clone();
                    tokio::spawn(async move {
                        if let Err(error) = service.run_refresh(&refresh_source, permit).await {
                            tracing::warn!(source = %refresh_source.key(), %error, "publication refresh failed; last-good retained");
                        }
                    });
                }
            }
            return Ok(PublicationOutput {
                artifact, stale, held: record.state.pending.is_some(),
                source_observed_at: record.state.source_observed_at,
            });
        }
        let permit = self.permit(&source).context("publication is being prepared or temporarily unavailable")?;
        // A disconnected first subscriber must not cancel a commit and release
        // the writer gate while its blocking filesystem operation is still live.
        let service = self.clone();
        let refresh_source = source.clone();
        let record = tokio::spawn(async move {
            service.run_refresh(&refresh_source, permit).await
        }).await.context("publication preparation task failed")??;
        Ok(PublicationOutput {
            artifact: record.artifacts.get(format).context("exporter did not produce an artifact")?.clone(),
            stale: now.saturating_sub(record.state.source_observed_at) >= self.config.refresh_interval.as_secs() as i64,
            held: false, source_observed_at: record.state.source_observed_at,
        })
    }

    async fn run_refresh(&self, source: &Source, permit: RefreshPermit) -> anyhow::Result<PublicationRecord> {
        let (mut gate, _worker) = permit;
        // Timeout only fetching. Cancelling a spawn_blocking atomic write could
        // release the single-writer lock while the write is still running.
        let previous = self.store.load(source).await?;
        let schedule = tokio::time::timeout(self.config.worker_timeout, self.provider.fetch(source))
            .await.context("publication source timed out")??;
        let record = self.prepare(previous.as_ref(), source.clone(), schedule, Utc::now().timestamp())?;
        self.store.save(source, &record).await?;
        gate.next_allowed = Instant::now() + self.config.refresh_interval;
        Ok(record)
    }

    pub(super) fn prepare(
        &self, previous: Option<&PublicationRecord>, source: Source, schedule: Schedule, now: i64,
    ) -> anyhow::Result<PublicationRecord> {
        let state = reconcile(previous.map(|p| &p.state), normalize(source, schedule)?, now,
            self.config.confirmation_seconds)?;
        let mut artifacts = previous.map(|p| p.artifacts.clone()).unwrap_or_default();
        let mut successful = 0;
        for exporter in self.registry.iter() {
            let id = exporter.format().id;
            match ExportArtifact::render(exporter, &state.snapshot, now) {
                Ok(mut artifact) => {
                    if let Some(old) = artifacts.get(id) {
                        if old.etag == artifact.etag { artifact.modified_at = old.modified_at; }
                    }
                    artifacts.insert(id.to_owned(), artifact);
                    successful += 1;
                }
                Err(error) => tracing::warn!(format = id, %error, "export failed; keeping its previous artifact"),
            }
        }
        ensure!(successful > 0, "all exporters failed; refusing to replace publication");
        if state.pending.is_some() {
            tracing::warn!(source = %state.snapshot.source.key(), "removals await an independent source observation");
        }
        Ok(PublicationRecord { schema_version: SCHEMA_VERSION, state, artifacts })
    }

    pub fn filename(&self, source: &Source, format: &str) -> Option<String> {
        self.registry.get(format).map(|e| format!("maiapp-{}.{}", digest(source.key()), e.format().extension))
    }
}
