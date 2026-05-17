use std::collections::BTreeMap;

use chrono::NaiveDate;
use serde::{Deserialize, Serialize};

#[derive(Clone, Debug, Deserialize, Serialize)]
pub struct Group {
    pub name: String,
    #[serde(rename = "fac", skip_serializing_if = "Option::is_none")]
    pub faculty: Option<String>,
    #[serde(rename = "level", skip_serializing_if = "Option::is_none")]
    pub education_level: Option<String>,
}

#[derive(Clone, Debug, Deserialize, Serialize, PartialEq, Eq)]
pub struct BaseScheduleId {
    pub id: String,
}

#[derive(Clone, Debug, Deserialize, Serialize, PartialEq, Eq)]
pub struct TeacherName {
    pub name: String,
}

#[derive(Clone, Debug, Deserialize, Serialize, PartialEq, Eq)]
pub struct TeacherUid {
    pub uid: String,
}

#[derive(Clone, Debug, Deserialize, Serialize, PartialEq, Eq)]
pub struct TeacherId {
    pub name: TeacherName,
    pub uid: TeacherUid,
}

#[derive(Clone, Debug, Deserialize, Serialize)]
pub struct Classroom {
    pub name: String,
    pub uid: String,
}

#[derive(Clone, Debug, Deserialize, Serialize)]
pub struct Schedule {
    pub name: String,
    pub id: BaseScheduleId,
    pub created: i64,
    pub cached: i64,
    pub days: Vec<ScheduleDay>,
}

#[derive(Clone, Debug, Deserialize, Serialize)]
pub struct ScheduleDay {
    pub date: NaiveDate,
    #[serde(rename = "day")]
    pub day_of_week: String,
    pub lessons: Vec<Lesson>,
}

#[derive(Clone, Debug, Deserialize, Serialize)]
pub struct Lesson {
    pub name: String,
    #[serde(rename = "time_start")]
    pub time_start: String,
    #[serde(rename = "time_end")]
    pub time_end: String,
    pub lectors: Vec<TeacherId>,
    #[serde(rename = "type")]
    pub lesson_type: String,
    pub day: NaiveDate,
    pub rooms: Vec<Classroom>,
    pub lms: String,
    pub teams: String,
    pub other: String,
    #[serde(rename = "timeRange")]
    pub time_range: String,
}

#[derive(Clone, Debug, Deserialize)]
pub struct RawScheduleDay {
    pub day: String,
    pub pairs: BTreeMap<String, serde_json::Value>,
}

#[derive(Clone, Debug, Deserialize)]
pub struct RawGroupLesson {
    pub lector: BTreeMap<String, String>,
    #[serde(rename = "type")]
    pub lesson_type: BTreeMap<String, i64>,
    pub room: BTreeMap<String, String>,
    pub lms: Option<String>,
    pub teams: Option<String>,
    pub other: Option<String>,
    #[serde(rename = "time_start")]
    pub time_start: String,
    #[serde(rename = "time_end")]
    pub time_end: String,
}

#[derive(Clone, Debug, Deserialize)]
pub struct RawTeacherLesson {
    pub name: String,
    pub groups: Vec<String>,
    pub types: Vec<String>,
    pub rooms: BTreeMap<String, String>,
    #[serde(rename = "time_start")]
    pub time_start: String,
    #[serde(rename = "time_end")]
    pub time_end: String,
}

#[derive(Clone, Debug, Deserialize, Serialize)]
pub struct ExlerFaculty {
    pub name: String,
    pub path: String,
}

#[derive(Clone, Debug, Deserialize, Serialize)]
pub struct ExlerTeacher {
    pub name: String,
    pub path: String,
    pub faculty: ExlerFaculty,
    #[serde(rename = "nameHash")]
    pub name_hash: i32,
}

#[derive(Clone, Debug, Deserialize, Serialize)]
pub struct ExlerTeacherInfo {
    pub name: String,
    pub link: String,
    pub faculty: Option<String>,
    pub department: Option<String>,
    pub photos: Vec<String>,
    #[serde(rename = "largePhotos")]
    pub large_photos: BTreeMap<String, String>,
    pub reviews: Vec<ExlerTeacherReview>,
}

#[derive(Clone, Debug, Deserialize, Serialize)]
pub struct ExlerTeacherReview {
    pub author: Option<String>,
    pub source: Option<String>,
    #[serde(rename = "publishTime")]
    pub publish_time: Option<String>,
    pub hypertext: Option<String>,
}

#[derive(Clone, Debug, Serialize)]
pub struct MaiDataManifest {
    pub version: u8,
    pub data: Vec<MaiDataItem>,
}

#[derive(Clone, Debug, Serialize)]
pub struct MaiDataItem {
    #[serde(rename = "type")]
    pub item_type: &'static str,
    pub name: &'static str,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub subtitle: Option<&'static str>,
    #[serde(rename = "forTeachers")]
    pub for_teachers: bool,
    #[serde(rename = "leadingIcon", skip_serializing_if = "Option::is_none")]
    pub leading_icon: Option<Asset>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub category: Option<&'static str>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub icon: Option<Asset>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub asset: Option<Asset>,
    pub accent: bool,
    #[serde(rename = "asset-night", skip_serializing_if = "Option::is_none")]
    pub asset_night: Option<Asset>,
}

#[derive(Clone, Debug, Serialize)]
#[serde(tag = "type")]
pub enum Asset {
    #[serde(rename = "relative")]
    Relative { url: &'static str },
    #[serde(rename = "text")]
    Text { text: &'static str },
    #[serde(rename = "web")]
    Web { text: &'static str },
}
