pub mod ical;

use anyhow::{Context, ensure};
use serde::{Deserialize, Serialize};

use super::domain::{ScheduleSnapshot, digest};

#[derive(Clone, Debug, Serialize)]
pub struct ExportFormat {
    pub id: &'static str,
    pub title: &'static str,
    pub description: &'static str,
    pub extension: &'static str,
    pub mime_type: &'static str,
    pub subscription: bool,
}

/// Pure format adapter: no HTTP, source fetching, persistence or recurrence inference.
pub trait ScheduleExporter: Send + Sync {
    fn format(&self) -> ExportFormat;
    fn render(&self, snapshot: &ScheduleSnapshot) -> anyhow::Result<Vec<u8>>;
}

pub struct ExportRegistry {
    exporters: Vec<Box<dyn ScheduleExporter>>,
}

impl Default for ExportRegistry {
    fn default() -> Self {
        Self { exporters: vec![Box::new(ical::IcalendarExporter)] }
    }
}

impl ExportRegistry {
    /// Register another statically linked format without modifying publication
    /// or the landing page. Invalid metadata must never reach HTTP headers/paths.
    pub fn register(&mut self, exporter: Box<dyn ScheduleExporter>) -> anyhow::Result<()> {
        let format = exporter.format();
        ensure!(regex::Regex::new(r"^[a-z0-9-]+$")?.is_match(format.id), "invalid exporter identifier");
        ensure!(regex::Regex::new(r"^[a-z0-9]+$")?.is_match(format.extension), "invalid file extension");
        ensure!(self.get(format.id).is_none(), "duplicate exporter identifier");
        axum::http::HeaderValue::from_str(format.mime_type).context("invalid exporter MIME type")?;
        self.exporters.push(exporter);
        Ok(())
    }

    pub fn formats(&self) -> Vec<ExportFormat> {
        self.exporters.iter().map(|e| e.format()).collect()
    }

    pub fn get(&self, id: &str) -> Option<&dyn ScheduleExporter> {
        self.exporters.iter().find(|e| e.format().id == id).map(Box::as_ref)
    }

    pub fn iter(&self) -> impl Iterator<Item = &dyn ScheduleExporter> {
        self.exporters.iter().map(Box::as_ref)
    }
}

#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct ExportArtifact {
    #[serde(with = "base64_bytes")]
    pub bytes: Vec<u8>,
    pub etag: String,
    pub modified_at: i64,
    pub snapshot_revision: u64,
}

impl ExportArtifact {
    pub fn render(exporter: &dyn ScheduleExporter, snapshot: &ScheduleSnapshot, now: i64) -> anyhow::Result<Self> {
        let bytes = exporter.render(snapshot)?;
        ensure!(!bytes.is_empty() && bytes.len() <= 8 * 1024 * 1024, "invalid artifact size");
        Ok(Self { etag: format!("\"{}\"", digest(&bytes)), bytes, modified_at: now,
            snapshot_revision: snapshot.revision })
    }

    pub fn validate(&self) -> anyhow::Result<()> {
        ensure!(!self.bytes.is_empty() && self.bytes.len() <= 8 * 1024 * 1024, "invalid stored artifact size");
        ensure!(self.etag == format!("\"{}\"", digest(&self.bytes)), "artifact checksum mismatch");
        chrono::DateTime::from_timestamp(self.modified_at, 0).context("invalid artifact date")?;
        Ok(())
    }
}

mod base64_bytes {
    use base64::{Engine, engine::general_purpose::STANDARD};
    use serde::{Deserialize, Deserializer, Serializer, de::Error};

    pub fn serialize<S: Serializer>(bytes: &[u8], serializer: S) -> Result<S::Ok, S::Error> {
        serializer.serialize_str(&STANDARD.encode(bytes))
    }

    pub fn deserialize<'de, D: Deserializer<'de>>(deserializer: D) -> Result<Vec<u8>, D::Error> {
        STANDARD.decode(String::deserialize(deserializer)?).map_err(D::Error::custom)
    }
}
