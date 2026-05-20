use std::sync::Arc;

use crate::{
    cache::Cached,
    errors::{AppError, AppResult},
    models::{Group, Schedule, TeacherId},
    repositories::mai::MaiRepository,
};

pub struct ScheduleService {
    repository: Arc<MaiRepository>,
}

impl ScheduleService {
    pub fn new(repository: Arc<MaiRepository>) -> Self {
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
