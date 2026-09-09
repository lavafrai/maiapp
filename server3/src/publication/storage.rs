use std::{collections::BTreeMap, io::Write, path::PathBuf, sync::atomic::{AtomicU64, Ordering}};

use anyhow::{Context, ensure};
use serde::{Deserialize, Serialize};

use super::{domain::{PublicationState, MAX_OCCURRENCES, SCHEMA_VERSION, Source, digest}, exporters::ExportArtifact};

#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct PublicationRecord {
    pub schema_version: u32,
    pub state: PublicationState,
    pub artifacts: BTreeMap<String, ExportArtifact>,
}

impl PublicationRecord {
    pub fn validate(&self, source: &Source) -> anyhow::Result<()> {
        ensure!(self.schema_version == SCHEMA_VERSION, "unsupported publication schema; migration required");
        ensure!(self.state.snapshot.source == *source, "stored publication source mismatch");
        ensure!(!self.state.snapshot.occurrences.is_empty(), "stored publication is empty");
        ensure!(Source::parse(source.kind_name(), &source.id)? == *source, "invalid stored source");
        let snapshot = &self.state.snapshot;
        ensure!(snapshot.occurrences.len() <= MAX_OCCURRENCES, "stored history is too large");
        ensure!(snapshot.revision > 0 && !snapshot.name.is_empty(), "invalid stored snapshot");
        ensure!(snapshot.coverage.from <= snapshot.coverage.through, "invalid stored coverage");
        ensure!(snapshot.timezone == "Europe/Moscow", "unsupported stored timezone");
        let mut ids = std::collections::BTreeSet::new();
        for event in &snapshot.occurrences {
            ensure!(ids.insert(&event.uid), "duplicate stored event identifier");
            ensure!(event.uid.ends_with("@maiapp.lavafrai.ru")
                && event.uid.len() == 51
                && event.uid.as_bytes()[..32].iter().all(|c| c.is_ascii_hexdigit()), "invalid stored event identifier");
            ensure!(event.data.end > event.data.start, "invalid stored event time");
            chrono::DateTime::from_timestamp(event.modified_at, 0).context("invalid stored event date")?;
        }
        ensure!(!self.artifacts.is_empty(), "stored publication has no artifacts");
        for artifact in self.artifacts.values() {
            artifact.validate()?;
            ensure!(artifact.snapshot_revision > 0 && artifact.snapshot_revision <= snapshot.revision,
                "invalid stored artifact revision");
        }
        Ok(())
    }
}

#[derive(Clone)]
pub struct PublicationStore {
    directory: PathBuf,
}

impl PublicationStore {
    pub fn new(directory: PathBuf) -> Self { Self { directory } }

    pub fn path(&self, source: &Source) -> PathBuf {
        // User input is never used as a filesystem path.
        self.directory.join(format!("{}.json", digest(source.key())))
    }

    pub async fn load(&self, source: &Source) -> anyhow::Result<Option<PublicationRecord>> {
        let path = self.path(source);
        let source = source.clone();
        tokio::task::spawn_blocking(move || {
            let metadata = match std::fs::metadata(&path) {
                Ok(metadata) => metadata,
                Err(error) if error.kind() == std::io::ErrorKind::NotFound => return Ok(None),
                Err(error) => return Err(error.into()),
            };
            ensure!(metadata.len() <= 32 * 1024 * 1024, "publication file is too large");
            let record: PublicationRecord = serde_json::from_slice(&std::fs::read(path)?)
                .context("invalid publication file; refusing to reset its identity history")?;
            record.validate(&source)?;
            Ok(Some(record))
        }).await.context("publication reader task failed")?
    }

    pub async fn save(&self, source: &Source, record: &PublicationRecord) -> anyhow::Result<()> {
        record.validate(source)?;
        let bytes = serde_json::to_vec(record)?;
        ensure!(bytes.len() <= 32 * 1024 * 1024, "publication file is too large");
        let path = self.path(source);
        let directory = self.directory.clone();
        tokio::task::spawn_blocking(move || -> anyhow::Result<()> {
            static NEXT_TEMP: AtomicU64 = AtomicU64::new(0);
            std::fs::create_dir_all(&directory)?;
            let temporary = directory.join(format!(".publication-{}-{}.tmp", std::process::id(),
                NEXT_TEMP.fetch_add(1, Ordering::Relaxed)));
            // If create_new fails, do not remove a temporary file we do not own.
            let mut file = std::fs::OpenOptions::new().create_new(true).write(true).open(&temporary)?;
            let result = (|| -> anyhow::Result<()> {
                file.write_all(&bytes)?;
                file.sync_all()?;
                drop(file);
                // Commit snapshot and all successful artifacts together. The
                // target is never truncated; failures before rename keep it intact.
                std::fs::rename(&temporary, &path)?;
                #[cfg(unix)]
                std::fs::File::open(&directory)?.sync_all()?;
                Ok(())
            })();
            if result.is_err() { let _ = std::fs::remove_file(&temporary); }
            result
        }).await.context("publication writer task failed")?
    }
}
