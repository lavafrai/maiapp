use std::{
    collections::BTreeMap,
    path::{Path, PathBuf},
    sync::RwLock,
};

use serde::{Deserialize, Serialize};

use crate::models::TeacherId;

const INDEX_VERSION: u32 = 1;
const INDEX_FILE: &str = "teachers-index.json";
const STALE_AFTER_DAYS: i64 = 90;
const FLUSH_AFTER_CHANGES: usize = 32;

#[derive(Clone, Debug, Deserialize, Serialize, PartialEq, Eq)]
pub struct TeacherRecord {
    pub name: String,
    pub uid: String,
    #[serde(rename = "lastSeen")]
    pub last_seen: i64,
}

#[derive(Deserialize, Serialize)]
struct IndexFile {
    version: u32,
    #[serde(rename = "updatedAt")]
    updated_at: i64,
    teachers: Vec<TeacherRecord>,
}

#[derive(Default)]
struct State {
    records: BTreeMap<String, TeacherRecord>,
    pending_changes: usize,
}

/// Постоянный справочник «ФИО → uuid»: наполняется расписаниями, которые сервер и так загружает.
///
/// Нужен, чтобы связывать преподавателя из каталога отзывов с его расписанием: uuid в данных МАИ
/// встречается только в расписаниях групп.
pub struct TeacherDirectory {
    path: PathBuf,
    state: RwLock<State>,
}

impl TeacherDirectory {
    pub fn load(cache_dir: &Path) -> Self {
        let path = cache_dir.join(INDEX_FILE);
        let records = match std::fs::read_to_string(&path) {
            Ok(text) => match serde_json::from_str::<IndexFile>(&text) {
                Ok(file) if file.version == INDEX_VERSION => fresh_records(file.teachers),
                Ok(file) => {
                    tracing::warn!(version = file.version, "unsupported teachers index version");
                    BTreeMap::new()
                }
                Err(error) => {
                    tracing::warn!("failed to parse teachers index: {error}");
                    BTreeMap::new()
                }
            },
            Err(error) if error.kind() == std::io::ErrorKind::NotFound => BTreeMap::new(),
            Err(error) => {
                tracing::warn!("failed to read teachers index: {error}");
                BTreeMap::new()
            }
        };

        Self {
            path,
            state: RwLock::new(State {
                records,
                pending_changes: 0,
            }),
        }
    }

    pub async fn remember(&self, teachers: &[TeacherId]) {
        let should_flush = {
            let mut state = self.state.write().unwrap_or_else(|error| error.into_inner());
            let now = chrono::Utc::now().timestamp();
            let mut changed = false;

            for teacher in teachers {
                if teacher.uid.uid == "00000000-0000-0000-0000-000000000000" {
                    continue;
                }

                let key = normalize_teacher_name(&teacher.name.name);
                match state.records.get_mut(&key) {
                    Some(record) => {
                        if record.uid != teacher.uid.uid {
                            tracing::warn!(
                                name = %teacher.name.name,
                                old = %record.uid,
                                new = %teacher.uid.uid,
                                "teacher uuid changed"
                            );
                            record.uid = teacher.uid.uid.clone();
                            changed = true;
                        }
                        if record.last_seen != now {
                            record.last_seen = now;
                            changed = true;
                        }
                    }
                    None => {
                        state.records.insert(
                            key,
                            TeacherRecord {
                                name: teacher.name.name.clone(),
                                uid: teacher.uid.uid.clone(),
                                last_seen: now,
                            },
                        );
                        changed = true;
                    }
                }
            }

            if changed {
                state.pending_changes += 1;
            }
            state.pending_changes >= FLUSH_AFTER_CHANGES
        };

        if should_flush {
            self.flush().await;
        }
    }

    /// Соответствие «ФИО → uuid»; null, если такого преподавателя в справочнике нет
    pub fn uid_by_name(&self, name: &str) -> Option<String> {
        let key = normalize_teacher_name(name);
        let state = self.state.read().unwrap_or_else(|error| error.into_inner());
        state.records.get(&key).map(|record| record.uid.clone())
    }

    pub fn records(&self) -> Vec<TeacherRecord> {
        let state = self.state.read().unwrap_or_else(|error| error.into_inner());
        state.records.values().cloned().collect()
    }

    pub async fn flush(&self) {
        let (text, pending) = {
            let mut state = self.state.write().unwrap_or_else(|error| error.into_inner());
            if state.pending_changes == 0 {
                return;
            }

            let file = IndexFile {
                version: INDEX_VERSION,
                updated_at: chrono::Utc::now().timestamp(),
                teachers: state.records.values().cloned().collect(),
            };
            match serde_json::to_string(&file) {
                Ok(text) => {
                    let pending = state.pending_changes;
                    state.pending_changes = 0;
                    (text, pending)
                }
                Err(error) => {
                    tracing::warn!("failed to encode teachers index: {error}");
                    return;
                }
            }
        };

        if let Err(error) = write_atomically(&self.path, &text).await {
            tracing::warn!("failed to persist teachers index: {error}");
            let mut state = self.state.write().unwrap_or_else(|error| error.into_inner());
            state.pending_changes += pending;
            return;
        }

        tracing::debug!(records = text.len(), "teachers index persisted");
    }
}

/// Имена сравниваются без регистра и без различия «ё»/«е»: в расписаниях и в каталоге отзывов
/// написание расходится
pub fn normalize_teacher_name(name: &str) -> String {
    name.to_lowercase()
        .replace('ё', "е")
        .split_whitespace()
        .collect::<Vec<_>>()
        .join(" ")
}

fn fresh_records(teachers: Vec<TeacherRecord>) -> BTreeMap<String, TeacherRecord> {
    let oldest = chrono::Utc::now().timestamp() - STALE_AFTER_DAYS * 24 * 60 * 60;
    teachers
        .into_iter()
        .filter(|record| record.last_seen > oldest)
        .map(|record| (normalize_teacher_name(&record.name), record))
        .collect()
}

async fn write_atomically(path: &Path, text: &str) -> anyhow::Result<()> {
    if let Some(dir) = path.parent() {
        tokio::fs::create_dir_all(dir).await?;
    }
    let tmp_path = path.with_extension("json.tmp");
    tokio::fs::write(&tmp_path, text).await?;
    tokio::fs::rename(tmp_path, path).await?;
    Ok(())
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::models::{TeacherName, TeacherUid};

    fn teacher(name: &str, uid: &str) -> TeacherId {
        TeacherId {
            name: TeacherName {
                name: name.to_owned(),
            },
            uid: TeacherUid {
                uid: uid.to_owned(),
            },
        }
    }

    fn temp_dir(name: &str) -> PathBuf {
        let dir = std::env::temp_dir().join(format!(
            "maiapp-teachers-{name}-{}",
            std::process::id()
        ));
        let _ = std::fs::remove_dir_all(&dir);
        std::fs::create_dir_all(&dir).unwrap();
        dir
    }

    #[test]
    fn normalizes_case_yo_and_spaces() {
        assert_eq!(
            normalize_teacher_name("  Осокин   Андрей  "),
            "осокин андрей"
        );
        assert_eq!(
            normalize_teacher_name("Королёв Пётр"),
            normalize_teacher_name("королев петр")
        );
    }

    #[tokio::test]
    async fn remembers_teachers_and_survives_reload() {
        let dir = temp_dir("reload");
        let directory = TeacherDirectory::load(&dir);
        directory
            .remember(&[
                teacher("Осокин Андрей Владимирович", "uid-1"),
                teacher("Нулевой Учитель", "00000000-0000-0000-0000-000000000000"),
            ])
            .await;
        directory.flush().await;

        let reloaded = TeacherDirectory::load(&dir);
        assert_eq!(
            reloaded.uid_by_name("Осокин Андрей Владимирович"),
            Some("uid-1".to_owned())
        );
        assert_eq!(reloaded.uid_by_name("Нулевой Учитель"), None);
        assert_eq!(reloaded.records().len(), 1);
    }

    #[tokio::test]
    async fn replaces_uuid_for_same_name() {
        let dir = temp_dir("replace");
        let directory = TeacherDirectory::load(&dir);
        directory
            .remember(&[teacher("Иванов Иван", "old-uid")])
            .await;
        directory
            .remember(&[teacher("Иванов Иван", "new-uid")])
            .await;

        assert_eq!(
            directory.uid_by_name("иванов иван"),
            Some("new-uid".to_owned())
        );
        assert_eq!(directory.records().len(), 1);
    }

    #[tokio::test]
    async fn drops_records_not_seen_for_three_months() {
        let dir = temp_dir("stale");
        let old = chrono::Utc::now().timestamp() - STALE_AFTER_DAYS * 24 * 60 * 60 - 1;
        let file = IndexFile {
            version: INDEX_VERSION,
            updated_at: old,
            teachers: vec![TeacherRecord {
                name: "Старый Преподаватель".to_owned(),
                uid: "old".to_owned(),
                last_seen: old,
            }],
        };
        std::fs::write(dir.join(INDEX_FILE), serde_json::to_string(&file).unwrap()).unwrap();

        let directory = TeacherDirectory::load(&dir);
        assert_eq!(directory.records().len(), 0);
    }

    #[tokio::test]
    async fn starts_empty_when_file_is_broken() {
        let dir = temp_dir("broken");
        std::fs::write(dir.join(INDEX_FILE), "{ not json").unwrap();

        let directory = TeacherDirectory::load(&dir);
        assert_eq!(directory.records().len(), 0);

        directory
            .remember(&[teacher("Петров Пётр", "uid-2")])
            .await;
        directory.flush().await;

        let reloaded = TeacherDirectory::load(&dir);
        assert_eq!(reloaded.records().len(), 1);
    }
}
