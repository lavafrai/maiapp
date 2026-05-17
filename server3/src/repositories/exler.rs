use std::{collections::BTreeMap, sync::Arc, time::Duration};

use anyhow::anyhow;
use encoding_rs::WINDOWS_1251;
use futures::{StreamExt, stream};
use regex::Regex;
use scraper::{Html, Selector};

use crate::{
    cache::{AsyncCache, Cached},
    config::AppConfig,
    models::*,
    telemetry::Telemetry,
};

const EXLER_BASE: &str = "https://mai-sten.online";

pub struct ExlerRepository {
    client: reqwest::Client,
    teachers: AsyncCache<Vec<ExlerTeacher>>,
    teacher_info: AsyncCache<ExlerTeacherInfo>,
}

impl ExlerRepository {
    pub fn new(client: reqwest::Client, config: &AppConfig, telemetry: Arc<Telemetry>) -> Self {
        Self {
            client: client.clone(),
            teachers: AsyncCache::new("exler-teachers", config, telemetry.clone()),
            teacher_info: AsyncCache::new("exler-teacher", config, telemetry),
        }
    }

    pub async fn teachers(&self) -> anyhow::Result<Cached<Vec<ExlerTeacher>>> {
        let client = self.client.clone();
        self.teachers
            .get_or_refresh("exler-teachers".to_owned(), move || async move {
                let repo = ExlerFetcher { client };
                let teachers = repo.teachers().await?;
                Ok(filter_banned(teachers))
            })
            .await
    }

    pub async fn teacher_info_by_name(
        &self,
        name: &str,
    ) -> anyhow::Result<Option<Cached<ExlerTeacherInfo>>> {
        let teacher = self
            .teachers()
            .await?
            .value
            .into_iter()
            .find(|teacher| teacher.name.to_lowercase() == name.to_lowercase());
        match teacher {
            Some(teacher) => {
                let client = self.client.clone();
                let cache_key = teacher.path.clone();
                Ok(Some(
                    self.teacher_info
                        .get_or_refresh(cache_key, move || async move {
                            let repo = ExlerFetcher { client };
                            repo.teacher_info(&teacher).await
                        })
                        .await?,
                ))
            }
            None => Ok(None),
        }
    }
}

struct ExlerFetcher {
    client: reqwest::Client,
}

impl ExlerFetcher {
    async fn teachers(&self) -> anyhow::Result<Vec<ExlerTeacher>> {
        let faculties = self.faculties().await?;
        let teachers = stream::iter(faculties)
            .map(|faculty| async move { self.teachers_for_faculty(faculty).await })
            .buffer_unordered(6)
            .collect::<Vec<_>>()
            .await
            .into_iter()
            .filter_map(|result| match result {
                Ok(teachers) => Some(teachers),
                Err(error) => {
                    tracing::warn!("failed to parse Exler faculty: {error}");
                    None
                }
            })
            .into_iter()
            .flatten()
            .fold(Vec::<ExlerTeacher>::new(), |mut acc, teacher| {
                if !acc
                    .iter()
                    .any(|known| known.path == teacher.path || known.name == teacher.name)
                {
                    acc.push(teacher);
                }
                acc
            });
        Ok(teachers)
    }

    async fn faculties(&self) -> anyhow::Result<Vec<ExlerFaculty>> {
        let html = self.http_get(&format!("{EXLER_BASE}/prepods/")).await?;
        let document = Html::parse_document(&html);
        let selector = Selector::parse("a").unwrap();
        let mut faculties = Vec::new();
        for link in document.select(&selector) {
            let Some(path) = link.value().attr("href") else {
                continue;
            };
            if !path.starts_with("/prepods/")
                || path == "/prepods/"
                || path.matches('/').count() != 3
            {
                continue;
            }
            let name = link.text().collect::<String>().trim().to_owned();
            if name.is_empty() || name.contains("Алфавит") {
                continue;
            }
            if !faculties
                .iter()
                .any(|faculty: &ExlerFaculty| faculty.path == path)
            {
                faculties.push(ExlerFaculty {
                    name,
                    path: path.to_owned(),
                });
            }
        }
        Ok(faculties)
    }

    async fn teachers_for_faculty(
        &self,
        faculty: ExlerFaculty,
    ) -> anyhow::Result<Vec<ExlerTeacher>> {
        let html = self
            .http_get(&format!("{EXLER_BASE}{}", faculty.path))
            .await?;
        let document = Html::parse_document(&html);
        let selector = Selector::parse("ol a").unwrap();
        let mut teachers = Vec::new();
        for link in document.select(&selector) {
            let Some(path) = link.value().attr("href") else {
                continue;
            };
            if !path.starts_with("/prepods/") {
                continue;
            }
            let name = link.text().collect::<String>().trim().to_owned();
            if name.split_whitespace().count() < 2 {
                continue;
            }
            teachers.push(ExlerTeacher {
                name_hash: teacher_name_hash(&name),
                name,
                path: path.to_owned(),
                faculty: faculty.clone(),
            });
        }
        Ok(teachers)
    }

    async fn teacher_info(&self, teacher: &ExlerTeacher) -> anyhow::Result<ExlerTeacherInfo> {
        let page_url = format!("{EXLER_BASE}{}", teacher.path);
        let html = self.http_get(&page_url).await?;
        let blocks = Regex::new(r"<!--subscribeBegin-->([\s\S]+?)<!--subscribeEnd-->")
            .unwrap()
            .captures_iter(&html)
            .map(|capture| capture.get(1).unwrap().as_str().to_owned())
            .collect::<Vec<_>>();
        let base = blocks.first().cloned().unwrap_or_default();
        let mut info = parse_base_teacher_info(teacher, &base);
        info.link = page_url.clone();
        info.photos = parse_photos(&html, &page_url);
        info.large_photos = parse_large_photos(&html, &page_url);
        info.reviews = blocks
            .into_iter()
            .skip(1)
            .filter_map(|block| parse_teacher_review(&block))
            .collect();
        Ok(info)
    }

    async fn http_get(&self, url: &str) -> anyhow::Result<String> {
        let mut last_error = None;
        for _ in 0..3 {
            match self.client.get(url).send().await {
                Ok(response) if response.status().is_success() => {
                    let bytes = response.bytes().await?;
                    let (text, _, _) = WINDOWS_1251.decode(&bytes);
                    return Ok(text.into_owned());
                }
                Ok(response) => {
                    last_error = Some(anyhow!("Failed to fetch {url}: {}", response.status()))
                }
                Err(error) => last_error = Some(error.into()),
            }
            tokio::time::sleep(Duration::from_secs(1)).await;
        }
        Err(last_error.unwrap_or_else(|| anyhow!("Failed to fetch {url}")))
    }
}

fn filter_banned(mut teachers: Vec<ExlerTeacher>) -> Vec<ExlerTeacher> {
    teachers.retain(|teacher| {
        !matches!(
            teacher.path.as_str(),
            "/prepods/03/chekanov/" | "/prepods/08/kunitsyn/" | "/prepods/war/pochuev/"
        )
    });
    teachers
}

fn parse_base_teacher_info(teacher: &ExlerTeacher, html: &str) -> ExlerTeacherInfo {
    let text = Html::parse_fragment(html)
        .root_element()
        .text()
        .collect::<Vec<_>>()
        .join("\n");
    let name = Regex::new(r##"<font color="#006699"><b>(.+?)</b></font>"##)
        .unwrap()
        .captures_iter(html)
        .last()
        .and_then(|capture| capture.get(1))
        .map(|match_| match_.as_str().trim().to_owned())
        .unwrap_or_else(|| teacher.name.clone());
    ExlerTeacherInfo {
        name,
        link: format!("{EXLER_BASE}{}", teacher.path),
        faculty: capture_line(&text, "Факультет:"),
        department: capture_line(&text, "Кафедра:"),
        photos: Vec::new(),
        large_photos: BTreeMap::new(),
        reviews: Vec::new(),
    }
}

fn parse_teacher_review(html: &str) -> Option<ExlerTeacherReview> {
    if html.contains("<table") || html.contains("<b>ФРАЗЫ</b>") {
        return None;
    }
    let normalized = html
        .replace("small&gt;", "<small>")
        .replace("&nbsp;", " ")
        .replace("<small><font color=\"#666666\"> </font></small>", "");
    let small_re = Regex::new(r"(?is)<small.*?</small>").unwrap();
    let review_meta = small_re
        .find_iter(&normalized)
        .map(|m| m.as_str())
        .collect::<Vec<_>>()
        .join("\n");
    if review_meta.trim().is_empty() {
        return None;
    }
    let review_text = small_re.replace_all(&normalized, "").trim().to_owned();
    Some(ExlerTeacherReview {
        author: capture_meta(&review_meta, &["Автор", "Авторы", "Записал", "Прислал"]),
        source: capture_meta(&review_meta, &["Источник", "Источники"]),
        publish_time: capture_meta(&review_meta, &["Опубликовано"]),
        hypertext: Some(review_text),
    })
}

fn capture_line(text: &str, prefix: &str) -> Option<String> {
    text.lines()
        .find_map(|line| line.trim().strip_prefix(prefix).map(str::trim))
        .filter(|value| !value.is_empty())
        .map(ToOwned::to_owned)
}

fn capture_meta(html: &str, keys: &[&str]) -> Option<String> {
    let decoded = html_escape::decode_html_entities(html).to_string();
    let text = Regex::new(r"(?is)<[^>]+>")
        .unwrap()
        .replace_all(&decoded, "\n")
        .to_string();
    for line in text.lines().map(str::trim) {
        for key in keys {
            if let Some(value) = line.strip_prefix(&format!("{key}:")) {
                let value = value.trim();
                if !value.is_empty() {
                    return Some(value.to_owned());
                }
            }
        }
    }
    None
}

fn parse_photos(html: &str, base_url: &str) -> Vec<String> {
    let document = Html::parse_document(html);
    let selector = Selector::parse("img").unwrap();
    document
        .select(&selector)
        .filter_map(|img| img.value().attr("src"))
        .filter(|src| !src.contains("Jeremy-Hillary-Boob-PhD_form-header.png"))
        .map(|src| global_url(base_url, src))
        .collect()
}

fn parse_large_photos(html: &str, base_url: &str) -> BTreeMap<String, String> {
    let document = Html::parse_document(html);
    let selector = Selector::parse("a").unwrap();
    let mut photos = BTreeMap::new();
    for anchor in document.select(&selector) {
        let Some(href) = anchor.value().attr("href") else {
            continue;
        };
        if !is_url_to_image(href) {
            continue;
        }
        let img_selector = Selector::parse("img").unwrap();
        for img in anchor.select(&img_selector) {
            let Some(src) = img.value().attr("src") else {
                continue;
            };
            if src.contains("Jeremy-Hillary-Boob-PhD_form-header.png") {
                continue;
            }
            photos.insert(global_url(base_url, src), global_url(base_url, href));
        }
    }
    photos
}

fn global_url(base_url: &str, path: &str) -> String {
    if path.starts_with("http://") || path.starts_with("https://") {
        return path.to_owned();
    }
    let base = reqwest::Url::parse(base_url).ok();
    base.and_then(|base| base.join(path).ok())
        .map(|url| url.to_string())
        .unwrap_or_else(|| path.to_owned())
}

fn is_url_to_image(url: &str) -> bool {
    matches!(
        url.rsplit('.').next().map(str::to_lowercase).as_deref(),
        Some("jpg" | "jpeg" | "png" | "gif" | "webp")
    )
}

fn teacher_name_hash(name: &str) -> i32 {
    java_hash_code(
        &name
            .trim()
            .split_whitespace()
            .collect::<Vec<_>>()
            .tap_sort()
            .join(" ")
            .to_lowercase()
            .replace('ё', "е"),
    )
}

trait TapSort {
    fn tap_sort(self) -> Self;
}

impl<'a> TapSort for Vec<&'a str> {
    fn tap_sort(mut self) -> Self {
        self.sort_unstable();
        self
    }
}

fn java_hash_code(value: &str) -> i32 {
    value.encode_utf16().fold(0i32, |hash, unit| {
        hash.wrapping_mul(31).wrapping_add(unit as i32)
    })
}
