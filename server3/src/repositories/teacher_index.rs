use std::collections::{BTreeMap, HashMap};

use anyhow::Context;
use chrono::{NaiveDate, NaiveTime, Utc};

use crate::models::{
    BaseScheduleId, Group, Lesson, Schedule, ScheduleDay, TeacherId, TeacherName, TeacherUid,
};

use super::mai::{MaiDataSource, parse_raw_schedule};

pub(super) struct TeacherIndex {
    pub cached_at: i64,
    teachers: Vec<TeacherId>,
    schedules: HashMap<String, Schedule>,
}

#[derive(PartialEq, Eq, PartialOrd, Ord)]
struct LessonKey {
    start: NaiveTime,
    end: NaiveTime,
    name: String,
    lesson_type: String,
    rooms: Vec<(String, String)>,
    lms: String,
    teams: String,
    other: String,
}

impl LessonKey {
    fn new(lesson: &Lesson) -> anyhow::Result<Self> {
        let mut rooms = lesson
            .rooms
            .iter()
            .map(|room| (room.uid.clone(), room.name.clone()))
            .collect::<Vec<_>>();
        rooms.sort();
        let time = |value: &str| {
            NaiveTime::parse_from_str(value, "%H:%M:%S")
                .or_else(|_| NaiveTime::parse_from_str(value, "%H:%M"))
                .with_context(|| format!("invalid lesson time: {value}"))
        };
        Ok(Self {
            start: time(&lesson.time_start.time)?,
            end: time(&lesson.time_end.time)?,
            name: lesson.name.clone(),
            lesson_type: lesson.lesson_type.clone(),
            rooms,
            lms: lesson.lms.clone(),
            teams: lesson.teams.clone(),
            other: lesson.other.clone(),
        })
    }
}

struct TeacherBuilder {
    teacher: TeacherId,
    days: BTreeMap<NaiveDate, (String, BTreeMap<LessonKey, Lesson>)>,
}

impl TeacherIndex {
    pub async fn build(source: &dyn MaiDataSource) -> anyhow::Result<Self> {
        let cached_at = source
            .cached_at()
            .await?
            .context("local dump update date is required")?;
        let text = source
            .read("groups.json")
            .await?
            .context("groups not found while indexing teachers")?;
        let groups: Vec<Group> = serde_json::from_str(&text)?;
        let mut builders: HashMap<String, TeacherBuilder> = HashMap::new();
        for group in groups {
            if group.name == "Для внеучебных мероприятий (служебная)"
            {
                continue;
            }
            let filename = format!("{:x}.json", md5::compute(&group.name));
            let text = source.read(&filename).await?.with_context(|| {
                format!(
                    "schedule for {} is missing while indexing teachers",
                    group.name
                )
            })?;
            let schedule = parse_raw_schedule(&text, false)
                .with_context(|| format!("failed to index teachers for {}", group.name))?;
            for day in schedule.days {
                for lesson in day.lessons {
                    for teacher in &lesson.lectors {
                        if teacher.uid.uid == "00000000-0000-0000-0000-000000000000" {
                            continue;
                        }
                        let builder =
                            builders.entry(teacher.uid.uid.clone()).or_insert_with(|| {
                                TeacherBuilder {
                                    teacher: teacher.clone(),
                                    days: BTreeMap::new(),
                                }
                            });
                        let (_, lessons) = builder
                            .days
                            .entry(day.date)
                            .or_insert_with(|| (day.day_of_week.clone(), BTreeMap::new()));
                        let entry = lessons.entry(LessonKey::new(&lesson)?).or_insert_with(|| {
                            let mut lesson = lesson.clone();
                            lesson.lectors.clear();
                            lesson
                        });
                        if !entry
                            .lectors
                            .iter()
                            .any(|known| known.uid.uid == group.name)
                        {
                            entry.lectors.push(TeacherId {
                                name: TeacherName {
                                    name: group.name.clone(),
                                },
                                uid: TeacherUid {
                                    uid: group.name.clone(),
                                },
                            });
                        }
                    }
                }
            }
        }
        anyhow::ensure!(
            source.cached_at().await? == Some(cached_at),
            "dump changed while indexing teachers; retry after the update finishes"
        );
        let created = Utc::now().timestamp();
        let mut teachers = Vec::with_capacity(builders.len());
        let mut schedules = HashMap::with_capacity(builders.len());
        for (uid, builder) in builders {
            let name = builder.teacher.name.name.clone();
            teachers.push(builder.teacher);
            let days = builder
                .days
                .into_iter()
                .map(|(date, (day_of_week, lessons))| {
                    let lessons = lessons
                        .into_values()
                        .map(|mut lesson| {
                            lesson.lectors.sort_by(|a, b| a.name.name.cmp(&b.name.name));
                            lesson
                        })
                        .collect();
                    ScheduleDay {
                        date,
                        day_of_week,
                        lessons,
                    }
                })
                .collect();
            schedules.insert(
                uid,
                Schedule {
                    name: name.clone(),
                    id: BaseScheduleId { id: name },
                    created,
                    cached: cached_at,
                    days,
                },
            );
        }
        teachers.sort_by(|a, b| {
            a.name
                .name
                .cmp(&b.name.name)
                .then(a.uid.uid.cmp(&b.uid.uid))
        });
        tracing::info!(
            teachers = teachers.len(),
            cached_at,
            "indexed local teacher schedules"
        );
        Ok(Self {
            cached_at,
            teachers,
            schedules,
        })
    }

    pub fn teachers(&self) -> Vec<TeacherId> {
        self.teachers.clone()
    }

    pub fn find(&self, id: &str) -> Option<(&str, &Schedule)> {
        if let Some((uid, schedule)) = self.schedules.get_key_value(id) {
            return Some((uid, schedule));
        }
        let teacher = self
            .teachers
            .iter()
            .find(|teacher| teacher.name.name == id)?;
        Some((&teacher.uid.uid, self.schedules.get(&teacher.uid.uid)?))
    }
}
