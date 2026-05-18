use std::{collections::{BTreeMap, HashSet}, sync::Arc, time::Duration};

use anyhow::anyhow;
use encoding_rs::WINDOWS_1251;
use futures::{StreamExt, stream};
use regex::Regex;
use scraper::{ElementRef, Html, Selector};

use crate::{
    cache::{AsyncCache, Cached},
    config::AppConfig,
    models::*,
    telemetry::Telemetry,
};

// #1: Kotlin использует mai-exler.ru, Rust ранее ошибочно использовал mai-sten.online
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
        let all_teachers: Vec<ExlerTeacher> = stream::iter(faculties)
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
            .flatten()
            .collect();

        // #11: Kotlin: .distinctBy { it.path }.distinctBy { it.name } — два последовательных прохода
        // Старый Rust: fold с условием OR — иной результат при частичных совпадениях
        let mut seen_paths = HashSet::new();
        let by_path: Vec<ExlerTeacher> = all_teachers
            .into_iter()
            .filter(|t| seen_paths.insert(t.path.clone()))
            .collect();

        let mut seen_names = HashSet::new();
        Ok(by_path
            .into_iter()
            .filter(|t| seen_names.insert(t.name.clone()))
            .collect())
    }

    async fn faculties(&self) -> anyhow::Result<Vec<ExlerFaculty>> {
        let html = self.http_get(&format!("{EXLER_BASE}/prepods/")).await?;
        let document = Html::parse_document(&html);

        // #2: Kotlin: selectFirst("body > center > table > ... > table > tbody")
        //   .children()                          ← итерация по <tr> строкам
        //   .filterNot { "Преподов всего" }      ← фильтр был пропущен в старом Rust
        //   .filterNot { "Алфавитный список..." }
        //   .map { selectFirst("td:nth-child(3) > div > b > a") }
        // Старый Rust: сканировал все <a> на странице, фильтровал по структуре href
        let tbody_sel = Selector::parse(
            "body > center > table > tbody > tr > td:nth-child(1) > table > tbody > tr > td \
             > div > table > tbody > tr > td > table > tbody",
        )
        .unwrap();
        let link_sel = Selector::parse("td:nth-child(3) > div > b > a").unwrap();

        let mut faculties = Vec::new();
        if let Some(tbody) = document.select(&tbody_sel).next() {
            for child in tbody.children() {
                let Some(child_el) = child.value().as_element() else {
                    continue;
                };
                if child_el.name() != "tr" {
                    continue;
                }
                let row = ElementRef::wrap(child).unwrap();
                let row_text = row.text().collect::<String>();
                // Kotlin: filterNot { it.text().contains("Преподов всего") }
                // Kotlin: filterNot { it.text().contains("Алфавитный список всех преподавателей") }
                if row_text.contains("Преподов всего")
                    || row_text.contains("Алфавитный список всех преподавателей")
                {
                    continue;
                }
                if let Some(link) = row.select(&link_sel).next() {
                    let name = link.text().collect::<String>().trim().to_owned();
                    let path = link.value().attr("href").unwrap_or("").to_owned();
                    if !name.is_empty() && !path.is_empty() {
                        faculties.push(ExlerFaculty { name, path });
                    }
                }
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
            .await;
        if let Err(_) = &html {
            return Ok(Vec::new());
        };

        let html = html?;
        let document = Html::parse_document(&html);

        // #3: Kotlin: selectFirst("body > center > table > ... > ol")
        //   .children()            ← <li> элементы
        //   .map { it.selectFirst("a") }
        // Старый Rust: "ol a" (любой <a> в любом <ol>) + лишний фильтр по числу слов
        let ol_sel = Selector::parse(
            "body > center > table > tbody > tr > td:nth-child(1) > table > tbody > tr > td > ol",
        )
        .unwrap();
        // children().selectFirst("a") ≡ li > a (первый <a> в каждом <li>)
        let a_sel = Selector::parse("li > a").unwrap();

        let mut teachers = Vec::new();
        if let Some(ol) = document.select(&ol_sel).next() {
            for link in ol.select(&a_sel) {
                let name = link.text().collect::<String>().trim().to_owned();
                let path = link.value().attr("href").unwrap_or("").to_owned();
                if name.is_empty() || path.is_empty() {
                    continue;
                }
                // #3: Kotlin НЕ фильтрует по числу слов (teacherNameMatcher объявлен, но не применялся)
                // Старый Rust: if name.split_whitespace().count() < 2 { continue; } — убрано
                teachers.push(ExlerTeacher {
                    name_hash: teacher_name_hash(&name),
                    name,
                    path,
                    faculty: faculty.clone(),
                });
            }
        }
        Ok(teachers)
    }

    async fn teacher_info(&self, teacher: &ExlerTeacher) -> anyhow::Result<ExlerTeacherInfo> {
        let page_url = format!("{EXLER_BASE}{}", teacher.path);
        let html = self.http_get(&page_url).await?;

        // #4: Kotlin: val reviewsElement = Ksoup.parse(reviewsPage)
        //   .select("body > center > table > ... > td")
        // Regex и photos применяются к reviewsElement (скоупированный HTML), не ко всей странице
        // Старый Rust: regex и photos применялись к полному HTML страницы
        let document = Html::parse_document(&html);
        let content_sel = Selector::parse(
            "body > center > table > tbody > tr > td:nth-child(1) > table > tbody > tr > td",
        )
        .unwrap();
        let content_html = document
            .select(&content_sel)
            .next()
            .map(|el| el.html())
            .unwrap_or_else(|| html.clone());

        // Kotlin: val reviewTexts = regex.findAll(reviewsElement.toString())
        let blocks = Regex::new(r"<!--subscribeBegin-->([\s\S]+?)<!--subscribeEnd-->")
            .unwrap()
            .captures_iter(&content_html)
            .map(|capture| capture.get(1).unwrap().as_str().to_owned())
            .collect::<Vec<_>>();

        let base = blocks.first().cloned().unwrap_or_default();
        let mut info = parse_base_teacher_info(teacher, &base);
        info.link = page_url.clone();
        // Kotlin: reviewsElement.select("img") — photos скоупированы на content element
        info.photos = parse_photos(&content_html, &page_url);
        info.large_photos = parse_large_photos(&content_html, &page_url);
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
    // Kotlin: val baseInfoElement = Ksoup.parse(text)
    //         val baseInfoElementText = baseInfoElement.wholeText()
    let doc = Html::parse_fragment(html);
    let text = doc.root_element().text().collect::<Vec<_>>().join("\n");

    // Kotlin: "<font color="#006699"><b>(.+?)</b></font>".toRegex()
    //   .findAll(text).lastOrNull()?.groupValues?.get(1)?.trim() ?: defaultName
    let name = Regex::new(r##"<font color="#006699"><b>(.+?)</b></font>"##)
        .unwrap()
        .captures_iter(html)
        .last()
        .and_then(|capture| capture.get(1))
        .map(|m| m.as_str().trim().to_owned())
        .unwrap_or_else(|| teacher.name.clone());

    ExlerTeacherInfo {
        name,
        link: format!("{EXLER_BASE}{}", teacher.path),
        // #10: Kotlin использует findAll().lastOrNull() — берёт ПОСЛЕДНЕЕ совпадение
        // Старый Rust: find_map (первое совпадение)
        faculty: capture_line(&text, "Факультет:"),
        department: capture_line(&text, "Кафедра:"),
        photos: Vec::new(),
        large_photos: BTreeMap::new(),
        reviews: Vec::new(),
    }
}

fn parse_teacher_review(html: &str) -> Option<ExlerTeacherReview> {
    // Kotlin: .also { if (it.contains("<table")) return null }
    if html.contains("<table") || html.contains("<b>ФРАЗЫ</b>") {
        return None;
    }

    // #9: Kotlin: .contextual(case = { trim().startsWith("small&gt;") }) { replace(...) }
    // Замена выполняется ТОЛЬКО если блок начинается с "small&gt;" (case: https://mai-exler.ru/prepods/02/uskova/)
    // Старый Rust: безусловный replace для всех вхождений
    let normalized = if html.trim_start().starts_with("small&gt;") {
        html.replace("small&gt;", "<small>")
    } else {
        html.to_owned()
    };
    let normalized = normalized
        .replace("&nbsp;", " ")
        .replace("<small><font color=\"#666666\"> </font></small>", "");

    // Kotlin: val reviewMeta = Ksoup.parse(normalizedText).select("small").joinToString("\n")
    // joinToString на Elements вызывает toString() → outer HTML каждого <small>
    let doc = Html::parse_fragment(&normalized);
    let small_sel = Selector::parse("small").unwrap();
    let review_meta = doc
        .select(&small_sel)
        .map(|el| el.html())
        .collect::<Vec<_>>()
        .join("\n");

    if review_meta.trim().is_empty() {
        return None;
    }

    // Kotlin: val reviewText = normalizedText.replace(reviewMeta, "").trim()
    // Используем regex-удаление — надёжнее, чем строковый replace после DOM-ресериализации
    let small_re = Regex::new(r"(?is)<small.*?</small>").unwrap();
    let review_text = small_re.replace_all(&normalized, "").trim().to_owned();

    // #6: Kotlin применяет HTML-aware regex к review_meta (HTML тегов <small>):
    //   "<b>Автор(ы)?:\s?+</b>([\s\S]+?)(<br>|</font>)" → group 2 (контент)
    //   "<b>Записал?:\s?+</b>([\s\S]+?)(<br>|</font>)"  → group 2 (в Kotlin — баг: возвращает терминатор)
    //   "<b>Прислал?:\s?+</b>([\s\S]+?)(<br>|</font>)"  → group 2 (та же ситуация)
    // В Rust используем non-capturing группы, чтобы group 1 всегда был контентом
    // Старый Rust: strip_all_tags → поиск "Ключ: значение" в plain text
    let author = capture_html_meta(
        &review_meta,
        &[
            r"(?i)<b>Автор(?:ы)?:\s*</b>([\s\S]+?)(?:<br>|</font>)",
            r"(?i)<b>Записал?:\s*</b>([\s\S]+?)(?:<br>|</font>)",
            r"(?i)<b>Прислал?:\s*</b>([\s\S]+?)(?:<br>|</font>)",
        ],
    );
    let source = capture_html_meta(
        &review_meta,
        &[r"(?i)<b>Источник(?:и)?:\s*</b>([\s\S]+?)(?:<br>|</font>)"],
    );
    let publish_time = capture_html_meta(
        &review_meta,
        &[r"(?i)<b>Опубликовано:\s*</b>([\s\S]+?)(?:<br>|</font>)"],
    );

    Some(ExlerTeacherReview {
        author,
        source,
        publish_time,
        hypertext: Some(review_text),
    })
}

/// Извлекает первую совпавшую группу 1 из первого паттерна, который даёт непустое значение.
/// Kotlin-эквивалент: regex.find(reviewMeta)?.groupValues?.get(N)?.trim()
fn capture_html_meta(html: &str, patterns: &[&str]) -> Option<String> {
    for pattern in patterns {
        if let Ok(re) = Regex::new(pattern) {
            if let Some(cap) = re.captures(html) {
                if let Some(m) = cap.get(1) {
                    let value = m.as_str().trim().to_owned();
                    if !value.is_empty() {
                        return Some(value);
                    }
                }
            }
        }
    }
    None
}

/// #10: Kotlin: "Факультет:(.+?)\n".toRegex().findAll(text).lastOrNull()?.groupValues?.get(1)?.trim()
/// Берёт ПОСЛЕДНЕЕ совпадение. Старый Rust: find_map (первое совпадение).
fn capture_line(text: &str, prefix: &str) -> Option<String> {
    text.lines()
        .filter_map(|line| {
            let trimmed = line.trim();
            trimmed.strip_prefix(prefix).map(|v| v.trim().to_owned())
        })
        .last()
        .filter(|value| !value.is_empty())
}

/// Kotlin: reviewsElement.select("img") — photos берутся из скоупированного content HTML
fn parse_photos(html: &str, base_url: &str) -> Vec<String> {
    let document = Html::parse_document(html);
    let selector = Selector::parse("img").unwrap();
    document
        .select(&selector)
        .filter_map(|img| img.value().attr("src"))
        .filter(|src| !src.contains("Jeremy-Hillary-Boob-PhD_form-header.png"))
        .filter(|src| !src.contains("spacer.gif"))
        .map(|src| global_url(base_url, src))
        .collect()
}

/// #8: Kotlin итерирует ВСЕ <img> и для каждого проверяет родителя:
///   если parent.hasAttr("href") && href.isUrlToImage() → large = href
///   иначе → large = src  (включает img, НЕ обёрнутые в image-ссылку)
/// Старый Rust: итерировал только <a href="*.jpg"> и брал вложенные img → пропускал не-linked imgs
fn parse_large_photos(html: &str, base_url: &str) -> BTreeMap<String, String> {
    let document = Html::parse_document(html);
    let img_selector = Selector::parse("img").unwrap();
    let mut photos = BTreeMap::new();

    for img in document.select(&img_selector) {
        let Some(src) = img.value().attr("src") else {
            continue;
        };
        if src.contains("Jeremy-Hillary-Boob-PhD_form-header.png") {
            continue;
        }
        // Kotlin: if (it.parent()?.hasAttr("href") == true && it.parent()?.attr("href")?.isUrlToImage() == true)
        //   → large = parent.attr("href")
        // else → large = src
        let large = img
            .parent()
            .and_then(|parent| parent.value().as_element())
            .and_then(|parent_el| parent_el.attr("href"))
            .filter(|href| is_url_to_image(href))
            .map(|href| global_url(base_url, href))
            .unwrap_or_else(|| global_url(base_url, src));

        photos.insert(global_url(base_url, src), large);
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
