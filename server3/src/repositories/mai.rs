use std::{collections::BTreeMap, sync::Arc};

use anyhow::{Context, anyhow};
use chrono::{NaiveDate, Utc};
use regex::Regex;
use tokio::sync::RwLock;

use crate::{
    cache::{AsyncCache, Cached},
    config::AppConfig,
    models::*,
    telemetry::Telemetry,
};

const GROUPS_URL: &str = "https://public.mai.ru/schedule/data/groups.json";
const SCHEDULE_BASE_URL: &str = "https://public.mai.ru/schedule/data";

pub struct MaiRepository {
    client: reqwest::Client,
    groups: AsyncCache<Vec<Group>>,
    schedules: AsyncCache<Schedule>,
    teachers: RwLock<Vec<TeacherId>>,
}

impl MaiRepository {
    pub fn new(client: reqwest::Client, config: &AppConfig, telemetry: Arc<Telemetry>) -> Self {
        Self {
            client: client.clone(),
            groups: AsyncCache::new("groups", config, telemetry.clone()),
            schedules: AsyncCache::new("schedule", config, telemetry),
            teachers: RwLock::new(Vec::new()),
        }
    }

    pub async fn groups(&self) -> anyhow::Result<Cached<Vec<Group>>> {
        let client = self.client.clone();
        self.groups
            .get_or_refresh("groups".to_owned(), move || async move {
                let mut groups = client
                    .get(GROUPS_URL)
                    .send()
                    .await?
                    .error_for_status()?
                    .json::<Vec<Group>>()
                    .await?;
                groups.retain(|g| g.name != "Для внеучебных мероприятий (служебная)");
                Ok(groups)
            })
            .await
    }

    pub async fn teachers(&self) -> Vec<TeacherId> {
        self.teachers.read().await.clone()
    }

    pub async fn schedule_by_id(&self, id: &str) -> anyhow::Result<Cached<Schedule>> {
        let schedule = if is_teacher_uid(id) {
            self.teacher_schedule(id).await?
        } else if is_group_name(id) {
            self.group_schedule(id).await?
        } else if is_teacher_name(id) {
            let teachers = self.teachers().await;
            let teacher = teachers
                .iter()
                .find(|teacher| teacher.name.name == id)
                .ok_or_else(|| {
                    anyhow!("schedule({id}) not found, teacher id is currently unknown")
                })?;
            self.teacher_schedule(&teacher.uid.uid).await?
        } else {
            anyhow::bail!("schedule({id}) not found, invalid format");
        };
        Ok(schedule)
    }

    async fn group_schedule(&self, group: &str) -> anyhow::Result<Cached<Schedule>> {
        let key = format!("group:{group}");
        let url = format!(
            "{SCHEDULE_BASE_URL}/{:x}.json",
            md5::compute(group.as_bytes())
        );
        let schedule = self.cached_schedule(key, url, false).await?;
        self.remember_teachers(&schedule.value).await;
        Ok(schedule)
    }

    async fn teacher_schedule(&self, uid: &str) -> anyhow::Result<Cached<Schedule>> {
        let key = format!("teacher:{uid}");
        let url = format!("{SCHEDULE_BASE_URL}/{uid}.json");
        self.cached_schedule(key, url, true).await
    }

    async fn cached_schedule(
        &self,
        key: String,
        url: String,
        teacher_mode_hint: bool,
    ) -> anyhow::Result<Cached<Schedule>> {
        let client = self.client.clone();
        self.schedules
            .get_or_refresh(key.clone(), move || async move {
                let response = client.get(url).send().await?;
                if response.status() == reqwest::StatusCode::NOT_FOUND {
                    anyhow::bail!("{} not found", key.replacen(':', "(", 1) + ")");
                }
                let response = response.error_for_status()?;
                let text = response.text().await?;
                parse_raw_schedule(&text, teacher_mode_hint)
            })
            .await
    }

    async fn remember_teachers(&self, schedule: &Schedule) {
        let mut teachers = self.teachers.write().await;
        for day in &schedule.days {
            for lesson in &day.lessons {
                for teacher in &lesson.lectors {
                    if teacher.uid.uid == "00000000-0000-0000-0000-000000000000" {
                        continue;
                    }
                    if !teachers.iter().any(|known| known.uid == teacher.uid) {
                        teachers.push(teacher.clone());
                    }
                }
            }
        }
    }
}

fn parse_raw_schedule(text: &str, teacher_mode_hint: bool) -> anyhow::Result<Schedule> {
    let root: serde_json::Value = serde_json::from_str(text)?;
    let object = root
        .as_object()
        .context("schedule json root is not an object")?;
    let name = object
        .get("group")
        .or_else(|| object.get("name"))
        .and_then(|value| value.as_str())
        .context("schedule json does not contain group/name")?
        .to_owned();
    let teacher_mode = teacher_mode_hint || object.contains_key("groups");
    let schedule_value = object.get("schedule").unwrap_or(&root);
    let schedule_object = schedule_value
        .as_object()
        .context("schedule body is not an object")?;
    let mut days = Vec::with_capacity(schedule_object.len());

    for (date_text, day_value) in schedule_object {
        if date_text == "group" || date_text == "name" || date_text == "groups" {
            continue;
        }
        let raw_day: RawScheduleDay = serde_json::from_value(day_value.clone())?;
        let date = parse_mai_date(date_text)?;
        let lessons = if teacher_mode {
            parse_teacher_lessons(&raw_day.pairs, date)?
        } else {
            parse_group_lessons(&raw_day.pairs, date)?
        };
        days.push(ScheduleDay {
            date,
            day_of_week: raw_day.day,
            lessons,
        });
    }

    Ok(Schedule {
        name: name.clone(),
        id: BaseScheduleId { id: name },
        created: Utc::now().timestamp(),
        cached: 0,
        days,
    })
}

fn parse_group_lessons(
    pairs: &BTreeMap<String, serde_json::Value>,
    date: NaiveDate,
) -> anyhow::Result<Vec<Lesson>> {
    let mut lessons = Vec::with_capacity(pairs.len());
    for value in pairs.values() {
        let object = value.as_object().context("group pair is not an object")?;
        let (name, raw_value) = object.iter().next().context("group pair is empty")?;
        let raw: RawGroupLesson = serde_json::from_value(raw_value.clone())?;
        lessons.push(Lesson {
            name: name.clone(),
            time_range: time_range(&raw.time_start, &raw.time_end),
            time_start: Time { time: raw.time_start },
            time_end: Time { time: raw.time_end },
            lectors: raw
                .lector
                .into_iter()
                .map(|(uid, name)| TeacherId {
                    name: TeacherName {
                        name: capitalize_words(&name),
                    },
                    uid: TeacherUid { uid },
                })
                .collect(),
            lesson_type: lesson_type(
                raw.lesson_type
                    .keys()
                    .next()
                    .map(String::as_str)
                    .unwrap_or_default(),
            ),
            day: date,
            rooms: raw
                .room
                .into_iter()
                .map(|(uid, name)| Classroom { name, uid })
                .collect(),
            lms: raw.lms.unwrap_or_default(),
            teams: raw.teams.unwrap_or_default(),
            other: raw.other.unwrap_or_default(),
        });
    }
    Ok(lessons)
}

fn parse_teacher_lessons(
    pairs: &BTreeMap<String, serde_json::Value>,
    date: NaiveDate,
) -> anyhow::Result<Vec<Lesson>> {
    let mut lessons = Vec::with_capacity(pairs.len());
    for value in pairs.values() {
        let raw: RawTeacherLesson = serde_json::from_value(value.clone())?;
        lessons.push(Lesson {
            name: raw.name,
            time_range: time_range(&raw.time_start, &raw.time_end),
            time_start: Time { time: raw.time_start },
            time_end: Time { time: raw.time_end },
            lectors: raw
                .groups
                .into_iter()
                .map(|group| TeacherId {
                    name: TeacherName {
                        name: group.clone(),
                    },
                    uid: TeacherUid { uid: group },
                })
                .collect(),
            lesson_type: lesson_type(raw.types.first().map(String::as_str).unwrap_or_default()),
            day: date,
            rooms: raw
                .rooms
                .into_iter()
                .map(|(uid, name)| Classroom { name, uid })
                .collect(),
            lms: String::new(),
            teams: String::new(),
            other: String::new(),
        });
    }
    Ok(lessons)
}

fn parse_mai_date(text: &str) -> anyhow::Result<NaiveDate> {
    let parts = text.split('.').collect::<Vec<_>>();
    if parts.len() != 3 {
        anyhow::bail!("invalid MAI date: {text}");
    }
    Ok(
        NaiveDate::from_ymd_opt(parts[2].parse()?, parts[1].parse()?, parts[0].parse()?)
            .ok_or_else(|| anyhow!("invalid MAI date: {text}"))?,
    )
}

fn time_range(start: &str, end: &str) -> String {
    format!("{} – {}", trim_time(start), trim_time(end))
}

fn trim_time(time: &str) -> String {
    time.rsplit_once(':')
        .map(|(head, _)| head)
        .unwrap_or(time)
        .to_owned()
}

fn lesson_type(raw: &str) -> String {
    match raw {
        "ЛК" | "ЛР" | "ПЗ" | "Экзамен" | "Встреча" => raw.to_owned(),
        _ => String::new(),
    }
}

fn capitalize_words(text: &str) -> String {
    text.split_whitespace()
        .map(|word| {
            let mut chars = word.chars();
            match chars.next() {
                Some(first) => {
                    first.to_uppercase().collect::<String>() + &chars.as_str().to_lowercase()
                }
                None => String::new(),
            }
        })
        .collect::<Vec<_>>()
        .join(" ")
}

fn is_group_name(value: &str) -> bool {
    Regex::new(r"^(([МТ])([\dИУ]+?)([ОВЗ]))-((\d+?)(Б|С|А|СВ|БВ|М)к?и?)-(\d+?)$")
        .unwrap()
        .is_match(value)
}

fn is_teacher_uid(value: &str) -> bool {
    Regex::new(r"^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")
        .unwrap()
        .is_match(value)
}

fn is_teacher_name(value: &str) -> bool {
    Regex::new(r"^([\SА-Яа-яЁё-]+?( |$)){3,5}$")
        .unwrap()
        .is_match(value)
}
