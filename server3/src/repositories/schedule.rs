use futures::future::BoxFuture;

use crate::{
    cache::Cached,
    models::{Group, Schedule, TeacherId},
};

pub trait ScheduleRepository: Send + Sync {
    fn groups(&self) -> BoxFuture<'_, anyhow::Result<Cached<Vec<Group>>>>;

    fn teachers(&self) -> BoxFuture<'_, anyhow::Result<Vec<TeacherId>>>;

    fn schedule_by_id<'a>(&'a self, id: &'a str)
    -> BoxFuture<'a, anyhow::Result<Cached<Schedule>>>;

    /// uuid преподавателя в расписании МАИ по ФИО; None, если его ещё не встречали
    fn teacher_uid<'a>(&'a self, name: &'a str) -> BoxFuture<'a, Option<String>> {
        let _ = name;
        Box::pin(async { None })
    }
}
