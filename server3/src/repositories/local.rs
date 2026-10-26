use std::{
    path::{Path, PathBuf},
    sync::Arc,
};

use anyhow::Context;
use chrono::{FixedOffset, NaiveDateTime};
use futures::future::BoxFuture;
use tokio::sync::{Mutex, MutexGuard};

use crate::{
    cache::{CacheStatus, Cached},
    config::AppConfig,
    models::{Group, Schedule, TeacherId},
    telemetry::Telemetry,
};

use super::{
    mai::{MaiDataSource, MaiRepository, is_group_name, is_teacher_name, is_teacher_uid},
    schedule::ScheduleRepository,
    teacher_index::TeacherIndex,
};

struct LocalMaiSource {
    data_dir: PathBuf,
    last_update_path: PathBuf,
}

impl MaiDataSource for LocalMaiSource {
    fn read<'a>(&'a self, filename: &'a str) -> BoxFuture<'a, anyhow::Result<Option<String>>> {
        Box::pin(async move {
            let path = self.data_dir.join(filename);
            match tokio::fs::read_to_string(&path).await {
                Ok(text) => Ok(Some(text)),
                Err(error) if error.kind() == std::io::ErrorKind::NotFound => Ok(None),
                Err(error) => {
                    Err(error).with_context(|| format!("failed to read {}", path.display()))
                }
            }
        })
    }

    fn cached_at(&self) -> BoxFuture<'_, anyhow::Result<Option<i64>>> {
        Box::pin(async move {
            let text = tokio::fs::read_to_string(&self.last_update_path)
                .await
                .with_context(|| {
                    format!(
                        "failed to read dump update date from {}",
                        self.last_update_path.display()
                    )
                })?;
            let date = NaiveDateTime::parse_from_str(text.trim(), "%Y-%m-%d %H:%M:%S")
                .with_context(|| format!("invalid dump update date in {}; expected YYYY-MM-DD HH:MM:SS (Europe/Moscow)", self.last_update_path.display()))?;
            let timestamp = date
                .and_local_timezone(FixedOffset::east_opt(3 * 60 * 60).unwrap())
                .single()
                .context("invalid dump update date")?
                .timestamp();
            Ok(Some(timestamp))
        })
    }
}

pub struct LocalMaiRepository {
    inner: MaiRepository,
    source: Arc<LocalMaiSource>,
    teacher_index: Mutex<TeacherIndex>,
}

impl LocalMaiRepository {
    pub async fn new(
        root: &Path,
        config: &AppConfig,
        telemetry: Arc<Telemetry>,
    ) -> anyhow::Result<Self> {
        let mut data_dir = None;
        for candidate in [
            root.to_path_buf(),
            root.join("data"),
            root.join("schedule/data"),
        ] {
            match tokio::fs::metadata(candidate.join("groups.json")).await {
                Ok(metadata) if metadata.is_file() => {
                    data_dir = Some(tokio::fs::canonicalize(candidate).await?);
                    break;
                }
                Ok(_) => {}
                Err(error) if error.kind() == std::io::ErrorKind::NotFound => {}
                Err(error) => {
                    return Err(error).context("failed to access local schedule directory");
                }
            }
        }
        let data_dir = data_dir.with_context(|| {
            format!(
                "groups.json not found in {}, data/ or schedule/data/",
                root.display()
            )
        })?;
        let mut update_paths = vec![data_dir.join("last_update.txt")];
        if let Some(parent) = data_dir.parent() {
            update_paths.push(parent.join("last_update.txt"));
            if parent.file_name().is_some_and(|name| name == "schedule") {
                if let Some(root) = parent.parent() {
                    update_paths.push(root.join("last_update.txt"));
                }
            }
        }
        let mut last_update_path = None;
        for path in update_paths {
            match tokio::fs::metadata(&path).await {
                Ok(metadata) if metadata.is_file() => {
                    last_update_path = Some(path);
                    break;
                }
                Ok(_) => {}
                Err(error) if error.kind() == std::io::ErrorKind::NotFound => {}
                Err(error) => return Err(error).context("failed to access dump update date"),
            }
        }
        let source = Arc::new(LocalMaiSource {
            last_update_path: last_update_path.context(
                "local dump requires last_update.txt in the data directory or dump root",
            )?,
            data_dir: data_dir.clone(),
        });
        let teacher_index = Mutex::new(TeacherIndex::build(source.as_ref()).await?);
        tracing::info!("using local schedules from {}", data_dir.display());
        let mut config = config.clone();
        config.cache_dir = config.cache_dir.join("local").join(format!(
            "{:x}",
            md5::compute(data_dir.as_os_str().as_encoded_bytes())
        ));
        Ok(Self {
            inner: MaiRepository::with_source(source.clone(), &config, telemetry),
            source,
            teacher_index,
        })
    }

    async fn current_teacher_index(&self) -> anyhow::Result<MutexGuard<'_, TeacherIndex>> {
        let cached_at = self
            .source
            .cached_at()
            .await?
            .context("local dump update date is required")?;
        let mut index = self.teacher_index.lock().await;
        if index.cached_at != cached_at {
            *index = TeacherIndex::build(self.source.as_ref()).await?;
        }
        Ok(index)
    }
}

impl ScheduleRepository for LocalMaiRepository {
    fn groups(&self) -> BoxFuture<'_, anyhow::Result<Cached<Vec<Group>>>> {
        Box::pin(self.inner.groups())
    }

    fn teachers(&self) -> BoxFuture<'_, anyhow::Result<Vec<TeacherId>>> {
        Box::pin(async { Ok(self.current_teacher_index().await?.teachers()) })
    }

    fn schedule_by_id<'a>(
        &'a self,
        id: &'a str,
    ) -> BoxFuture<'a, anyhow::Result<Cached<Schedule>>> {
        Box::pin(async move {
            if is_group_name(id) {
                return self.inner.schedule_by_id(id).await;
            }
            let index = self.current_teacher_index().await?;
            let indexed = index.find(id);
            if indexed.is_none() && !(is_teacher_uid(id) || is_teacher_name(id)) {
                drop(index);
                return self.inner.schedule_by_id(id).await;
            }
            let uid = indexed.map(|(uid, _)| uid).unwrap_or(id).to_owned();
            let schedule = indexed.map(|(_, schedule)| schedule.clone());
            drop(index);
            if is_teacher_uid(&uid)
                && tokio::fs::try_exists(self.source.data_dir.join(format!("{uid}.json"))).await?
            {
                return self.inner.schedule_by_id(&uid).await;
            }
            let schedule = schedule
                .with_context(|| format!("schedule({id}) not found in the local teacher index"))?;
            Ok(Cached {
                value: schedule,
                status: CacheStatus::Hit,
            })
        })
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::cache::{AsyncCache, CacheStatus};
    use serde_json::json;
    use std::sync::atomic::{AtomicU64, Ordering};

    const GROUP: &str = "М4О-103БВ-24";
    const TEACHER: &str = "12345678-1234-1234-1234-123456789abc";
    const DUMP_CACHED_AT: i64 = 1_792_836_000;
    static NEXT_ID: AtomicU64 = AtomicU64::new(0);

    struct Fixture {
        root: PathBuf,
        config: AppConfig,
    }

    impl Fixture {
        fn new() -> Self {
            let root = std::env::current_dir()
                .unwrap()
                .join("target")
                .join(format!(
                    "local-repository-test-{}-{}",
                    std::process::id(),
                    NEXT_ID.fetch_add(1, Ordering::Relaxed)
                ));
            std::fs::create_dir_all(root.join("data")).unwrap();
            let mut config = AppConfig::from_env().unwrap();
            config.cache_dir = root.join("cache");
            config.telemetry_path = root.join("telemetry.json");
            let fixture = Self { root, config };
            std::fs::write(fixture.root.join("last_update.txt"), "2026-10-24 13:00:00").unwrap();
            fixture.write(
                "groups.json",
                json!([
                    {"name": GROUP, "fac": "Институт №4"},
                    {"name": "Для внеучебных мероприятий (служебная)"}
                ]),
            );
            fixture.write(
                &format!("{:x}.json", md5::compute(GROUP)),
                json!({
                    "group": GROUP,
                    "01.09.2026": {"day": "Вт", "pairs": {"09:00:00": {
                        "Математика": {
                            "time_start": "09:00:00", "time_end": "10:30:00",
                            "lector": {TEACHER: "ИВАНОВ ИВАН ИВАНОВИЧ"},
                            "type": {"ЛК": 1}, "room": {"room-id": "ГУК А-101"}
                        }
                    }}}
                }),
            );
            fixture.write(
                &format!("{TEACHER}.json"),
                json!({
                    "name": "Иванов Иван Иванович", "groups": [GROUP],
                    "01.09.2026": {"day": "Вт", "pairs": {"09:00:00": {
                        "name": "Математика", "time_start": "09:00:00", "time_end": "10:30:00",
                        "groups": [GROUP], "types": ["ЛК"], "rooms": {"room-id": "ГУК А-101"}
                    }}}
                }),
            );
            fixture
        }

        fn write(&self, filename: &str, value: serde_json::Value) {
            std::fs::write(self.root.join("data").join(filename), value.to_string()).unwrap();
        }

        async fn repository(&self, path: &Path) -> LocalMaiRepository {
            let telemetry = Arc::new(Telemetry::load_or_new(&self.config).await);
            LocalMaiRepository::new(path, &self.config, telemetry)
                .await
                .unwrap()
        }
    }

    impl Drop for Fixture {
        fn drop(&mut self) {
            let _ = std::fs::remove_dir_all(&self.root);
        }
    }

    #[tokio::test]
    async fn reads_group_and_teacher_schedules_from_dump() {
        let fixture = Fixture::new();
        let repository = fixture.repository(&fixture.root).await;
        assert_eq!(repository.teachers().await.unwrap()[0].uid.uid, TEACHER);
        let groups = repository.groups().await.unwrap();
        assert_eq!(groups.value.len(), 1);
        assert_eq!(groups.value[0].name, GROUP);
        assert_eq!(groups.status, CacheStatus::Miss);
        assert_eq!(repository.groups().await.unwrap().status, CacheStatus::Hit);

        let schedule = repository.schedule_by_id(GROUP).await.unwrap();
        assert_eq!(schedule.value.cached, DUMP_CACHED_AT);
        assert_eq!(schedule.value.name, GROUP);
        let lesson = &schedule.value.days[0].lessons[0];
        assert_eq!(lesson.name, "Математика");
        assert_eq!(lesson.lectors[0].uid.uid, TEACHER);
        assert_eq!(lesson.time_range, "09:00 – 10:30");
        assert_eq!(
            repository.teachers().await.unwrap()[0].name.name,
            "Иванов Иван Иванович"
        );

        for id in [TEACHER, "Иванов Иван Иванович"] {
            let schedule = repository.schedule_by_id(id).await.unwrap();
            assert_eq!(schedule.value.cached, DUMP_CACHED_AT);
            assert_eq!(schedule.value.name, "Иванов Иван Иванович");
            assert_eq!(
                schedule.value.days[0].lessons[0].lectors[0].name.name,
                GROUP
            );
        }
        let direct = fixture.repository(&fixture.root.join("data")).await;
        let restored = direct.schedule_by_id(GROUP).await.unwrap();
        assert_eq!(restored.status, CacheStatus::Hit);
        assert_eq!(restored.value.cached, DUMP_CACHED_AT);
    }

    #[tokio::test]
    async fn indexes_teachers_at_startup_without_uuid_files_and_merges_joint_lessons() {
        let fixture = Fixture::new();
        let group2 = "М4О-104БВ-24";
        let group3 = "М4О-105БВ-24";
        let teacher2 = "87654321-4321-4321-4321-cba987654321";
        std::fs::remove_file(fixture.root.join("data").join(format!("{TEACHER}.json"))).unwrap();
        fixture.write(
            "groups.json",
            json!([{"name": GROUP}, {"name": group2}, {"name": group3}]),
        );
        let filename = format!("{:x}.json", md5::compute(GROUP));
        let mut raw: serde_json::Value = serde_json::from_str(
            &std::fs::read_to_string(fixture.root.join("data").join(&filename)).unwrap(),
        )
        .unwrap();
        for (name, room) in [(group2, "ГУК А-101"), (group3, "ГУК А-102")] {
            let mut copy = raw.clone();
            copy["group"] = json!(name);
            copy["01.09.2026"]["pairs"]["09:00:00"]["Математика"]["time_start"] = json!("9:00:00");
            copy["01.09.2026"]["pairs"]["09:00:00"]["Математика"]["room"]["room-id"] = json!(room);
            fixture.write(&format!("{:x}.json", md5::compute(name)), copy);
        }
        let mut algebra = raw["01.09.2026"]["pairs"]["09:00:00"]["Математика"].clone();
        algebra["time_start"] = json!("10:45:00");
        algebra["time_end"] = json!("12:15:00");
        let mut physics = algebra.clone();
        physics["lector"] = json!({teacher2: "МАНУН МОХСЕН"});
        raw["01.09.2026"]["pairs"]["10:45:00"] = json!({"Алгебра": algebra, "Физика": physics});
        let mut unknown = raw["01.09.2026"]["pairs"]["09:00:00"]["Математика"].clone();
        unknown["lector"] = json!({"00000000-0000-0000-0000-000000000000": ""});
        raw["01.09.2026"]["pairs"]["13:00:00"] = json!({"Без преподавателя": unknown});
        fixture.write(&filename, raw);

        let repository = fixture.repository(&fixture.root).await;
        let teachers = repository.teachers().await.unwrap();
        assert_eq!(teachers.len(), 2);
        assert!(teachers.iter().any(|teacher| teacher.uid.uid == TEACHER));
        for id in [TEACHER, "Иванов Иван Иванович"] {
            let schedule = repository.schedule_by_id(id).await.unwrap();
            assert_eq!(schedule.value.cached, DUMP_CACHED_AT);
            let lessons = &schedule.value.days[0].lessons;
            assert_eq!(lessons.len(), 3);
            assert_eq!(lessons[0].name, "Математика");
            assert_eq!(
                lessons[0]
                    .lectors
                    .iter()
                    .map(|group| group.name.name.as_str())
                    .collect::<Vec<_>>(),
                [GROUP, group2]
            );
            assert_eq!(lessons[1].rooms[0].name, "ГУК А-102");
            assert_eq!(lessons[1].lectors[0].name.name, group3);
            assert_eq!(lessons[2].name, "Алгебра");
        }
        for id in [teacher2, "Манун Мохсен"] {
            let schedule = repository.schedule_by_id(id).await.unwrap();
            assert_eq!(schedule.value.days[0].lessons[0].name, "Физика");
        }
        assert_eq!(
            repository.schedule_by_id(GROUP).await.unwrap().value.days[0]
                .lessons
                .len(),
            4
        );
    }

    #[tokio::test]
    async fn rebuilds_teacher_index_after_dump_update_and_reports_incomplete_dumps() {
        let fixture = Fixture::new();
        std::fs::remove_file(fixture.root.join("data").join(format!("{TEACHER}.json"))).unwrap();
        let repository = fixture.repository(&fixture.root).await;
        assert_eq!(repository.teachers().await.unwrap().len(), 1);
        let filename = format!("{:x}.json", md5::compute(GROUP));
        std::fs::remove_file(fixture.root.join("data").join(&filename)).unwrap();
        std::fs::write(fixture.root.join("last_update.txt"), "2026-10-25 13:00:00").unwrap();
        assert!(repository.teachers().await.is_err());
        assert!(repository.schedule_by_id(TEACHER).await.is_err());

        fixture.write(&filename, json!({"group": GROUP}));
        assert!(repository.teachers().await.unwrap().is_empty());
        assert!(
            repository
                .schedule_by_id(TEACHER)
                .await
                .unwrap_err()
                .to_string()
                .contains("not found")
        );

        std::fs::remove_file(fixture.root.join("data").join(&filename)).unwrap();
        let telemetry = Arc::new(Telemetry::load_or_new(&fixture.config).await);
        assert!(
            LocalMaiRepository::new(&fixture.root, &fixture.config, telemetry)
                .await
                .is_err()
        );
    }

    #[tokio::test]
    async fn invalidates_cached_data_when_dump_update_date_changes() {
        let fixture = Fixture::new();
        let repository = fixture.repository(&fixture.root).await;
        repository.groups().await.unwrap();
        assert_eq!(
            repository.schedule_by_id(GROUP).await.unwrap().value.cached,
            DUMP_CACHED_AT
        );
        assert_eq!(
            repository.schedule_by_id(GROUP).await.unwrap().status,
            CacheStatus::Hit
        );

        fixture.write(
            "groups.json",
            json!([{"name": GROUP, "fac": "Updated faculty"}]),
        );
        fixture.write(
            &format!("{:x}.json", md5::compute(GROUP)),
            json!({"group": GROUP}),
        );
        std::fs::write(
            fixture.root.join("last_update.txt"),
            "2026-10-25 13:00:00\n",
        )
        .unwrap();

        let updated = repository.schedule_by_id(GROUP).await.unwrap();
        assert_eq!(updated.status, CacheStatus::Miss);
        assert_eq!(updated.value.cached, DUMP_CACHED_AT + 86_400);
        assert!(updated.value.days.is_empty());
        let groups = repository.groups().await.unwrap();
        assert_eq!(groups.status, CacheStatus::Miss);
        assert_eq!(groups.value[0].faculty.as_deref(), Some("Updated faculty"));

        let restarted = fixture.repository(&fixture.root).await;
        let restored = restarted.schedule_by_id(GROUP).await.unwrap();
        assert_eq!(restored.status, CacheStatus::Hit);
        assert_eq!(restored.value.cached, DUMP_CACHED_AT + 86_400);
        assert!(restored.value.days.is_empty());
    }

    #[tokio::test]
    async fn rejects_missing_or_invalid_dump_update_dates_even_on_cache_hits() {
        let fixture = Fixture::new();
        let repository = fixture.repository(&fixture.root).await;
        repository.schedule_by_id(GROUP).await.unwrap();
        for date in ["", "2026-02-30 13:00:00", "not a date"] {
            std::fs::write(fixture.root.join("last_update.txt"), date).unwrap();
            let error = repository.schedule_by_id(GROUP).await.unwrap_err();
            assert!(error.to_string().contains("invalid dump update date"));
            assert!(repository.groups().await.is_err());
            let telemetry = Arc::new(Telemetry::load_or_new(&fixture.config).await);
            assert!(
                LocalMaiRepository::new(&fixture.root, &fixture.config, telemetry)
                    .await
                    .is_err()
            );
        }
        std::fs::remove_file(fixture.root.join("last_update.txt")).unwrap();
        assert!(repository.schedule_by_id(GROUP).await.is_err());
        let telemetry = Arc::new(Telemetry::load_or_new(&fixture.config).await);
        assert!(
            LocalMaiRepository::new(&fixture.root, &fixture.config, telemetry)
                .await
                .is_err()
        );
    }

    #[tokio::test]
    async fn finds_update_dates_in_supported_dump_layouts() {
        let fixture = Fixture::new();
        std::fs::rename(
            fixture.root.join("last_update.txt"),
            fixture.root.join("data/last_update.txt"),
        )
        .unwrap();
        let repository = fixture.repository(&fixture.root.join("data")).await;
        assert_eq!(
            repository.schedule_by_id(GROUP).await.unwrap().value.cached,
            DUMP_CACHED_AT
        );

        let site = Fixture::new();
        std::fs::create_dir(site.root.join("schedule")).unwrap();
        std::fs::rename(site.root.join("data"), site.root.join("schedule/data")).unwrap();
        let repository = site.repository(&site.root).await;
        assert_eq!(
            repository.schedule_by_id(GROUP).await.unwrap().value.cached,
            DUMP_CACHED_AT
        );
    }

    #[tokio::test]
    async fn handles_missing_and_invalid_files_without_network_fallback() {
        let fixture = Fixture::new();
        let repository = fixture.repository(&fixture.root).await;
        let missing = repository.schedule_by_id("М4О-999БВ-24").await.unwrap_err();
        assert!(missing.to_string().contains("not found"));
        let invalid = repository
            .schedule_by_id("../../groups.json")
            .await
            .unwrap_err();
        assert!(invalid.to_string().contains("invalid format"));
        std::fs::remove_file(fixture.root.join("data").join(format!("{TEACHER}.json"))).unwrap();
        assert_eq!(
            repository.schedule_by_id(TEACHER).await.unwrap().value.name,
            "Иванов Иван Иванович"
        );

        std::fs::write(
            fixture
                .root
                .join("data")
                .join(format!("{:x}.json", md5::compute(GROUP))),
            "invalid json",
        )
        .unwrap();
        assert!(repository.schedule_by_id(GROUP).await.is_err());
        fixture.write("groups.json", json!({"invalid": "groups"}));
        assert!(repository.groups().await.is_err());
        let telemetry = Arc::new(Telemetry::load_or_new(&fixture.config).await);
        assert!(
            LocalMaiRepository::new(&fixture.root.join("missing"), &fixture.config, telemetry)
                .await
                .is_err()
        );
    }

    #[tokio::test]
    async fn isolates_cache_from_remote_and_other_dumps_and_refreshes_files() {
        let mut fixture = Fixture::new();
        let telemetry = Arc::new(Telemetry::load_or_new(&fixture.config).await);
        let remote = AsyncCache::<Vec<Group>>::new("groups", &fixture.config, telemetry);
        remote
            .get_or_refresh("groups".to_owned(), || async { Ok(Vec::new()) })
            .await
            .unwrap();
        fixture.write("groups.json", json!([{"name": "Local group"}]));
        fixture.write(
            &format!("{:x}.json", md5::compute("Local group")),
            json!({"group": "Local group"}),
        );
        let repository = fixture.repository(&fixture.root).await;
        assert_eq!(repository.groups().await.unwrap().value.len(), 1);
        assert_eq!(
            repository.groups().await.unwrap().value[0].name,
            "Local group"
        );

        let other = Fixture::new();
        let telemetry = Arc::new(Telemetry::load_or_new(&fixture.config).await);
        let other_repository = LocalMaiRepository::new(&other.root, &fixture.config, telemetry)
            .await
            .unwrap();
        assert_eq!(
            other_repository.groups().await.unwrap().value[0].name,
            GROUP
        );

        fixture.config.cache_fresh_ttl = std::time::Duration::ZERO;
        fixture.config.cache_valid_ttl = std::time::Duration::ZERO;
        let repository = fixture.repository(&fixture.root).await;
        assert_eq!(
            repository.groups().await.unwrap().value[0].name,
            "Local group"
        );
        fixture.write("groups.json", json!([{"name": "Updated group"}]));
        assert_eq!(
            repository.groups().await.unwrap().value[0].name,
            "Updated group"
        );
    }
}
