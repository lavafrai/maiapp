use std::sync::Arc;

use crate::{
    cache::Cached,
    errors::{AppError, AppResult},
    models::{ExlerTeacher, ExlerTeacherInfo},
    repositories::exler::ExlerRepository,
};

pub struct ExlerService {
    repository: Arc<ExlerRepository>,
}

impl ExlerService {
    pub fn new(repository: Arc<ExlerRepository>) -> Self {
        Self { repository }
    }

    pub async fn teachers(&self) -> AppResult<Cached<Vec<ExlerTeacher>>> {
        Ok(self.repository.teachers().await?)
    }

    pub async fn teacher_info(&self, name: &str) -> AppResult<Cached<ExlerTeacherInfo>> {
        self.repository
            .teacher_info_by_name(name)
            .await?
            .ok_or_else(|| AppError::not_found("Teacher not found"))
    }
}
