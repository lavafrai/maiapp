use std::sync::Arc;

use crate::{
    cache::Cached,
    errors::{AppError, AppResult},
    models::{Group, Schedule, TeacherId},
    repositories::schedule::ScheduleRepository,
};

pub struct ScheduleService {
    repository: Arc<dyn ScheduleRepository>,
}

impl ScheduleService {
    pub fn new(repository: Arc<dyn ScheduleRepository>) -> Self {
        Self { repository }
    }

    pub async fn groups(&self) -> AppResult<Cached<Vec<Group>>> {
        Ok(self.repository.groups().await?)
    }

    pub async fn teachers(&self) -> Vec<TeacherId> {
        self.repository.teachers().await
    }

    pub async fn schedule(&self, id: &str) -> AppResult<Cached<Schedule>> {
        self.repository.schedule_by_id(id).await.map_err(|error| {
            let message = error.to_string();
            if message.contains("not found") || message.contains("invalid format") {
                AppError::not_found(message)
            } else {
                AppError::from(error)
            }
        })
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::{
        cache::CacheStatus,
        models::{BaseScheduleId, TeacherName, TeacherUid},
    };
    use futures::future::BoxFuture;

    struct TestRepository {
        schedule_error: Option<&'static str>,
    }

    impl ScheduleRepository for TestRepository {
        fn groups(&self) -> BoxFuture<'_, anyhow::Result<Cached<Vec<Group>>>> {
            Box::pin(async {
                Ok(Cached {
                    value: vec![Group {
                        name: "custom group".to_owned(),
                        faculty: None,
                        education_level: None,
                    }],
                    status: CacheStatus::Stale,
                })
            })
        }

        fn teachers(&self) -> BoxFuture<'_, Vec<TeacherId>> {
            Box::pin(async {
                vec![TeacherId {
                    name: TeacherName {
                        name: "custom teacher".to_owned(),
                    },
                    uid: TeacherUid {
                        uid: "custom uid".to_owned(),
                    },
                }]
            })
        }

        fn schedule_by_id<'a>(
            &'a self,
            id: &'a str,
        ) -> BoxFuture<'a, anyhow::Result<Cached<Schedule>>> {
            Box::pin(async move {
                if let Some(message) = self.schedule_error {
                    anyhow::bail!(message);
                }
                Ok(Cached {
                    value: Schedule {
                        name: "custom schedule".to_owned(),
                        id: BaseScheduleId { id: id.to_owned() },
                        created: 0,
                        cached: 0,
                        days: Vec::new(),
                    },
                    status: CacheStatus::Miss,
                })
            })
        }
    }

    #[tokio::test]
    async fn accepts_an_alternative_repository_with_its_own_ids() {
        let service = ScheduleService::new(Arc::new(TestRepository {
            schedule_error: None,
        }));

        let groups = service.groups().await.unwrap();
        assert_eq!(groups.value[0].name, "custom group");
        assert_eq!(groups.status, CacheStatus::Stale);

        let teachers = service.teachers().await;
        assert_eq!(teachers[0].name.name, "custom teacher");
        assert_eq!(teachers[0].uid.uid, "custom uid");

        let schedule = service.schedule("custom-id").await.unwrap();
        assert_eq!(schedule.value.id.id, "custom-id");
        assert_eq!(schedule.value.name, "custom schedule");
        assert_eq!(schedule.status, CacheStatus::Miss);
    }

    #[tokio::test]
    async fn preserves_error_mapping_for_alternative_repositories() {
        for message in ["schedule not found", "invalid format", "connection failed"] {
            let service = ScheduleService::new(Arc::new(TestRepository {
                schedule_error: Some(message),
            }));
            let error = service.schedule("custom-id").await.unwrap_err();
            if message == "connection failed" {
                assert!(matches!(error, AppError::Upstream(_)));
            } else {
                assert!(matches!(error, AppError::NotFound(_)));
            }
            assert_eq!(error.to_string(), message);
        }
    }
}
