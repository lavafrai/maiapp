use futures::future::BoxFuture;

use crate::{
    cache::Cached,
    models::{Group, Schedule, TeacherId},
};

pub trait ScheduleRepository: Send + Sync {
    fn groups(&self) -> BoxFuture<'_, anyhow::Result<Cached<Vec<Group>>>>;

    fn teachers(&self) -> BoxFuture<'_, Vec<TeacherId>>;

    fn schedule_by_id<'a>(&'a self, id: &'a str)
    -> BoxFuture<'a, anyhow::Result<Cached<Schedule>>>;
}
