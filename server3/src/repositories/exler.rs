use std::{
    collections::{BTreeMap, HashSet},
    sync::Arc,
    time::Duration,
};

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

// Сайт с отзывами: mai-sten.online (прежний адрес — mai-exler.ru, он больше не работает)
const EXLER_BASE: &str = "https://mai-sten.online";

/// Hosts of teacher photos. Browsers can't load them in the web version directly
/// (these sites don't send CORS headers), so they are proxied from here
const PHOTO_HOSTS: [&str; 3] = ["mai-sten.online", "mai-exler.ru", "www.mai-exler.ru"];
const MAX_PHOTO_BYTES: usize = 10 * 1024 * 1024;

pub struct ExlerPhoto {
    pub content_type: String,
    pub bytes: Vec<u8>,
}

/// Only https teacher photos of the known hosts, so that the proxy can't be used for anything else
pub fn allowed_photo_url(url: &str) -> Option<reqwest::Url> {
    let url = reqwest::Url::parse(url).ok()?;
    let allowed = url.scheme() == "https"
        && url.port().is_none()
        && url.username().is_empty()
        && url.password().is_none()
        && url.host_str().is_some_and(|host| PHOTO_HOSTS.contains(&host))
        && url.path().starts_with("/prepods/");
    allowed.then_some(url)
}

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

    pub async fn photo(&self, url: reqwest::Url) -> anyhow::Result<ExlerPhoto> {
        let response = self
            .client
            .get(url)
            .timeout(Duration::from_secs(20))
            .send()
            .await?
            .error_for_status()?;

        let content_type = response
            .headers()
            .get(reqwest::header::CONTENT_TYPE)
            .and_then(|value| value.to_str().ok())
            .unwrap_or_default()
            .to_owned();
        if !content_type.starts_with("image/") {
            return Err(anyhow!("Not an image: {content_type}"));
        }
        if response.content_length().is_some_and(|length| length > MAX_PHOTO_BYTES as u64) {
            return Err(anyhow!("Photo is too large"));
        }

        let bytes = response.bytes().await?;
        if bytes.len() > MAX_PHOTO_BYTES {
            return Err(anyhow!("Photo is too large"));
        }
        Ok(ExlerPhoto { content_type, bytes: bytes.to_vec() })
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
        Ok(parse_teacher_list(&html, &faculty)
            .into_iter()
            .map(|(name, path)| ExlerTeacher {
                name_hash: teacher_name_hash(&name),
                name,
                path,
                faculty: faculty.clone(),
            })
            .collect())
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
    let text = inline_text(html);
    let places = parse_places(&text);

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
        // Старые поля — последнее место работы, чтобы клиенты без support of places не ломались
        faculty: places.iter().rev().find_map(|place| place.faculty.clone()),
        department: places.iter().rev().find_map(|place| place.department.clone()),
        unit: places.iter().rev().find_map(|place| place.unit.clone()),
        places,
        photos: Vec::new(),
        large_photos: BTreeMap::new(),
        reviews: Vec::new(),
    }
}

/// Места работы со страницы: «Факультет» начинает новый блок, «Кафедра» и «Подразделение» дополняют его
fn parse_places(text: &str) -> Vec<ExlerTeacherPlace> {
    let mut places: Vec<ExlerTeacherPlace> = Vec::new();
    let mut current: Option<ExlerTeacherPlace> = None;

    for line in text.lines() {
        let line = line.trim();
        let value = |prefix: &str| {
            line.strip_prefix(prefix)
                .map(|value| value.trim().replace('\u{a0}', " "))
                .filter(|value| !value.is_empty())
        };

        if let Some(faculty) = value("Факультет:") {
            if let Some(place) = current.take() {
                places.push(place);
            }
            current = Some(ExlerTeacherPlace {
                faculty: Some(faculty),
                department: None,
                unit: None,
            });
        } else if let Some(department) = value("Кафедра:") {
            current
                .get_or_insert_with(ExlerTeacherPlace::default)
                .department = Some(department);
        } else if let Some(unit) =
            value("Подразделение:").or_else(|| value("Подготовительные курсы:"))
        {
            current.get_or_insert_with(ExlerTeacherPlace::default).unit = Some(unit);
        }
    }
    if let Some(place) = current {
        places.push(place);
    }

    places.retain(|place| place.faculty.is_some() || place.department.is_some() || place.unit.is_some());
    places
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

/// Страницы подписаны по-разному: у школ «Подразделение», у подготовительных курсов свои подписи.
/// Зачёркнутый текст (`<strike>`, `<s>`, `<del>`) пропускается: это старые подразделения
fn inline_text(html: &str) -> String {
    let mut text = String::with_capacity(html.len());
    let mut rest = html;
    let mut struck_depth = 0usize;

    while let Some(start) = rest.find('<') {
        if struck_depth == 0 {
            text.push_str(&rest[..start]);
        }
        let Some(end) = rest[start..].find('>') else {
            rest = "";
            break;
        };
        let tag = rest[start + 1..start + end].trim();
        rest = &rest[start + end + 1..];

        let closing = tag.starts_with('/');
        let name = tag
            .trim_start_matches('/')
            .split(|c: char| c.is_whitespace() || c == '/')
            .next()
            .unwrap_or_default()
            .to_ascii_lowercase();

        if matches!(name.as_str(), "strike" | "s" | "del") {
            if closing {
                struck_depth = struck_depth.saturating_sub(1);
            } else {
                struck_depth += 1;
            }
        } else if name == "br" && struck_depth == 0 {
            text.push('\n');
        }
    }
    if struck_depth == 0 {
        text.push_str(rest);
    }

    html_escape::decode_html_entities(&text).into_owned()
}

fn parse_photos(html: &str, base_url: &str) -> Vec<String> {
    let document = Html::parse_document(html);
    let selector = Selector::parse("img").unwrap();
    document
        .select(&selector)
        .filter_map(|img| img.value().attr("src"))
        .map(|src| global_url(base_url, src))
        .filter(|url| is_teacher_photo_url(url))
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
        let src_url = global_url(base_url, src);
        if !is_teacher_photo_url(&src_url) {
            continue;
        }

        let large = img
            .parent()
            .and_then(|parent| parent.value().as_element())
            .and_then(|parent_el| parent_el.attr("href"))
            .map(|href| global_url(base_url, href))
            .filter(|url| is_teacher_photo_url(url))
            .or_else(|| full_size_photo_name(&src_url))
            .unwrap_or_else(|| src_url.clone());

        photos.insert(src_url, large);
    }
    photos
}

/// По конвенции сайта крупная версия — то же имя без `_small` перед расширением
fn full_size_photo_name(url: &str) -> Option<String> {
    let mut parsed = reqwest::Url::parse(url).ok()?;
    let path = parsed.path();
    let (without_extension, extension) = path.rsplit_once('.')?;
    let without_suffix = without_extension.strip_suffix("_small")?;

    parsed.set_path(&format!("{without_suffix}.{extension}"));
    Some(parsed.to_string())
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

fn is_image_path(path: &str) -> bool {
    let path = path.split(|character| character == '?' || character == '#').next().unwrap_or(path);
    match path.rsplit('.').next() {
        Some(extension) => matches!(
            extension.to_lowercase().as_str(),
            "jpg" | "jpeg" | "png" | "gif" | "webp"
        ),
        None => false,
    }
}

/// Фотографии лежат в каталоге преподавателя и начинаются с `prepods_`; иконки, подписи и общие картинки сайта — нет
fn is_teacher_photo_url(url: &str) -> bool {
    let Some(parsed) = allowed_photo_url(url) else {
        return false;
    };

    let path = parsed.path();
    let Some(inside_prepods) = path.strip_prefix("/prepods/") else {
        return false;
    };
    let Some(file_name) = path.rsplit('/').next() else {
        return false;
    };

    inside_prepods.matches('/').count() >= 2 && file_name.starts_with("prepods_") && is_image_path(path)
}

/// Преподаватель лежит на `/prepods/<факультет>/<человек>/`, у страницы факультета сегментов меньше.
/// Так в каталог не попадают перекрёстные ссылки вида «Война» со страницы другого факультета
fn is_teacher_path(path: &str) -> bool {
    path.starts_with("/prepods/")
        && path
            .trim_matches('/')
            .split('/')
            .filter(|segment| !segment.is_empty())
            .count()
            == 3
}

/// В списке попадаются относительные ссылки (`boldinov/`), их надо достроить до полного пути
fn resolve_teacher_path(faculty_path: &str, href: &str) -> Option<String> {
    let path = if href.starts_with('/') {
        href.to_owned()
    } else if href.starts_with("http") {
        return None;
    } else {
        format!("{faculty_path}{}", href.trim_start_matches("./"))
    };

    if is_image_path(&path) || !is_teacher_path(&path) {
        return None;
    }
    Some(path)
}

/// Список преподавателей на странице факультета: `<li>` без закрывающих тегов, перед именем бывает иконка
fn parse_teacher_list(html: &str, faculty: &ExlerFaculty) -> Vec<(String, String)> {
    let document = Html::parse_document(html);
    // #3: Kotlin: selectFirst("body > center > table > ... > ol")
    //   .children()            ← <li> элементы
    //   .map { it.selectFirst("a") }
    let ol_sel = Selector::parse(
        "body > center > table > tbody > tr > td:nth-child(1) > table > tbody > tr > td > ol",
    )
    .unwrap();
    let li_sel = Selector::parse("li").unwrap();
    let a_sel = Selector::parse("a").unwrap();

    let mut teachers = Vec::new();
    let Some(ol) = document.select(&ol_sel).next() else {
        return teachers;
    };

    for item in ol.select(&li_sel) {
        // children().selectFirst("a") ≡ первая ссылка с текстом: перед ней в <li> бывает иконка ⊞
        let Some(link) = item
            .select(&a_sel)
            .find(|link| !link.text().collect::<String>().trim().is_empty())
        else {
            continue;
        };
        let name = link.text().collect::<String>().trim().to_owned();
        let Some(path) = resolve_teacher_path(&faculty.path, link.value().attr("href").unwrap_or(""))
        else {
            continue;
        };
        teachers.push((name, path));
    }
    teachers
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

#[cfg(test)]
mod teacher_info_tests {
    use super::{
        is_teacher_path, parse_base_teacher_info, parse_teacher_list, resolve_teacher_path,
    };
    use crate::models::{ExlerFaculty, ExlerTeacher};

    fn teacher(name: &str) -> ExlerTeacher {
        ExlerTeacher {
            name: name.to_owned(),
            path: "/prepods/02/uskova/".to_owned(),
            faculty: ExlerFaculty {
                name: "Факультет №2".to_owned(),
                path: "/prepods/02/".to_owned(),
            },
            name_hash: 0,
        }
    }

    #[test]
    fn reads_faculty_and_department() {
        let html = r##"<small><font color="#666666"><b>Факультет:</b> Факультет №8 «Аэрокосмический»<br><b>Кафедра:</b> 802 «Проектирование авиационных двигателей»<br></font></small>"##;

        let info = parse_base_teacher_info(&teacher("Осокин Андрей Владимирович"), html);

        assert_eq!(info.faculty.as_deref(), Some("Факультет №8 «Аэрокосмический»"));
        assert_eq!(info.department.as_deref(), Some("802 «Проектирование авиационных двигателей»"));
        assert_eq!(info.unit, None);
    }

    #[test]
    fn reads_unit_of_school() {
        let html = r##"<small><font color="#666666"><b>Подразделение:</b> школа «Предуниверсарий&nbsp;МАИ» (ПУМ)<br><br></font></small>"##;

        let info = parse_base_teacher_info(&teacher("Курочкина Ирина Юрьевна"), html);

        assert_eq!(info.unit.as_deref(), Some("школа «Предуниверсарий МАИ» (ПУМ)"));
        assert_eq!(info.faculty, None);
        assert_eq!(info.department, None);
    }

    #[test]
    fn reads_unit_of_preparatory_courses() {
        let html = r##"<small><font color="#666666"><b>Подготовительные курсы:</b> «МАИНО»<br><br></font></small>"##;

        let info = parse_base_teacher_info(&teacher("Гринченко Борис Исакович"), html);

        assert_eq!(info.unit.as_deref(), Some("«МАИНО»"));
        assert_eq!(info.faculty, None);
        assert_eq!(info.department, None);
    }

    #[test]
    fn parses_teacher_list_with_icons_and_cross_links() {
        let html = r##"<html><body><center><table><tbody><tr><td><table><tbody><tr><td>
<ol>
<li><img src="/prepods/icon_camera.png">&nbsp;<a href="/prepods/war/artamonov/">Артамонов Андрей Валентинович</a>, каф. ОВП
<li><img src="/prepods/icon_camera.png">&nbsp;<a href="/prepods/war/pochuev/">Почуев Сергей Анатольевич</a>, цикл <a title="цикл">[№ 1]</a>
<li><a href="/prepods/war/korobovsky/">Коробковский Александр Валентинович</a>, цикл <a title="цикл">[№ 4]</a>
<li><a href="/prepods/war/">«Война»</a>
</ol>
</td></tr></tbody></table></td></tr></tbody></table></center></body></html>"##;
        let faculty = ExlerFaculty {
            name: "Война".to_owned(),
            path: "/prepods/war/".to_owned(),
        };

        let teachers = parse_teacher_list(html, &faculty);

        assert_eq!(
            teachers
                .iter()
                .map(|(_, path)| path.as_str())
                .collect::<Vec<_>>(),
            vec![
                "/prepods/war/artamonov/",
                "/prepods/war/pochuev/",
                "/prepods/war/korobovsky/",
            ]
        );
    }

    #[test]
    fn reads_every_place_of_work() {
        let html = r##"<small><font color="#666666"><b>Факультет:</b> № 7, «Робототехнические и интеллектуальные системы» (ФАУСТ)<br><b>Кафедра:</b> 701, «Авиационные робототехнические системы»<br><br><b>Факультет:</b> «Война»: Военный учебный центр (ВУЦ) МАИ<br><b>Кафедра:</b> АВ, «Авиационное вооружение»<br><b>Звание:</b> подполковник<br></font></small>"##;

        let info = parse_base_teacher_info(&teacher("Болдинов Виктор Александрович"), html);

        assert_eq!(info.places.len(), 2);
        assert_eq!(
            info.places[0].faculty.as_deref(),
            Some("№ 7, «Робототехнические и интеллектуальные системы» (ФАУСТ)")
        );
        assert_eq!(
            info.places[0].department.as_deref(),
            Some("701, «Авиационные робототехнические системы»")
        );
        assert_eq!(
            info.places[1].faculty.as_deref(),
            Some("«Война»: Военный учебный центр (ВУЦ) МАИ")
        );
        assert_eq!(
            info.places[1].department.as_deref(),
            Some("АВ, «Авиационное вооружение»")
        );
        assert_eq!(info.faculty.as_deref(), info.places[1].faculty.as_deref());
        assert_eq!(info.department.as_deref(), info.places[1].department.as_deref());
    }

    #[test]
    fn ignores_struck_places() {
        let html = r##"<small><font color="#666666"><strike><b>Факультет:</b> № 10, «[Социальный инжиниринг]»</strike><br><strike><b>Кафедра:</b> 009 (позже 514, ныне 920), «Социология»</strike><br><b>Факультет:</b> № 5, «Экономика»<br><b>Кафедра:</b> 514, «Социология»<br></font></small>"##;

        let info = parse_base_teacher_info(&teacher("Ластовенко Дарья Викторовна"), html);

        assert_eq!(info.places.len(), 1);
        assert_eq!(info.places[0].faculty.as_deref(), Some("№ 5, «Экономика»"));
        assert_eq!(info.places[0].department.as_deref(), Some("514, «Социология»"));
    }

    #[test]
    fn ignores_struck_value_without_label() {
        let html = r##"<small><b>Факультет:</b> <s>№ 10, «[Социальный инжиниринг]»</s><br><b>Кафедра:</b> 920, «Социология»<br></small>"##;

        let info = parse_base_teacher_info(&teacher("Ластовенко Дарья Викторовна"), html);

        assert_eq!(info.places.len(), 1);
        assert_eq!(info.places[0].faculty, None);
        assert_eq!(info.places[0].department.as_deref(), Some("920, «Социология»"));
    }

    #[test]
    fn takes_name_from_page_header() {
        let html = r##"<big><font color="#006699"><b>Ирина Юрьевна Курочкина</b></font></big>"##;

        let info = parse_base_teacher_info(&teacher("Курочкина И.Ю."), html);

        assert_eq!(info.name, "Ирина Юрьевна Курочкина");
    }

    #[test]
    fn accepts_only_teacher_paths() {
        assert!(is_teacher_path("/prepods/09/ukhvakov/"));
        assert!(is_teacher_path("/prepods/;-)/Grinchenko_B_I__Kak_reshat_zadachi_po_fizike_1998.djvu"));

        assert!(!is_teacher_path("/prepods/war/"));
        assert!(!is_teacher_path("/prepods/"));
        assert!(!is_teacher_path("https://example.com/prepods/09/ukhvakov/"));
    }

    #[test]
    fn resolves_relative_teacher_links() {
        assert_eq!(
            resolve_teacher_path("/prepods/war/", "boldinov/").as_deref(),
            Some("/prepods/war/boldinov/")
        );
        assert_eq!(
            resolve_teacher_path("/prepods/war/", "/prepods/war/orlov/").as_deref(),
            Some("/prepods/war/orlov/")
        );

        assert_eq!(resolve_teacher_path("/prepods/war/", "prepods_war_boldinov.jpg"), None);
        assert_eq!(resolve_teacher_path("/prepods/war/", "https://example.com/x/"), None);
        assert_eq!(resolve_teacher_path("/prepods/war/", "/prepods/war/"), None);
    }
}

#[cfg(test)]
mod photo_url_tests {
    use super::{allowed_photo_url, full_size_photo_name, is_teacher_photo_url, parse_large_photos, parse_photos};

    #[test]
    fn allows_teacher_photos_of_exler_hosts() {
        assert!(allowed_photo_url("https://mai-sten.online/prepods/06/sergeev/prepods_603_sergeev_small.jpg").is_some());
        assert!(allowed_photo_url("https://mai-exler.ru/prepods/02/uskova/photo.jpg").is_some());
        assert!(allowed_photo_url("https://www.mai-exler.ru/prepods/02/uskova/photo.jpg").is_some());
    }

    #[test]
    fn rejects_everything_else() {
        for url in [
            "http://mai-sten.online/prepods/06/sergeev/photo.jpg",
            "https://example.com/prepods/photo.jpg",
            "https://mai-sten.online.example.com/prepods/photo.jpg",
            "https://mai-sten.online/admin/photo.jpg",
            "https://mai-sten.online:8443/prepods/photo.jpg",
            "https://user:pass@mai-sten.online/prepods/photo.jpg",
            "not a url",
        ] {
            assert!(allowed_photo_url(url).is_none(), "{url}");
        }
    }

    #[test]
    fn recognizes_teacher_photo_urls() {
        assert!(is_teacher_photo_url(
            "https://mai-sten.online/prepods/08/osokin/prepods_804_osokin_small.jpg"
        ));
        assert!(is_teacher_photo_url(
            "https://mai-sten.online/prepods/08/vestyak/prepods_311_vestyak_5_small.jpg"
        ));
        assert!(is_teacher_photo_url(
            "https://mai-sten.online/prepods/08/osokin/prepods_804_osokin_small.JPG"
        ));
        assert!(is_teacher_photo_url(
            "https://mai-sten.online/prepods/08/osokin/prepods_804_osokin_small.jpg?v=2"
        ));
    }

    #[test]
    fn ignores_site_icons_and_attachments() {
        for url in [
            "https://mai-sten.online/images/download_icon.gif",
            "https://mai-sten.online/images/file_type_MP3.gif",
            "https://mai-sten.online/images/Telegram_logo_2019_inverted.gif",
            "https://mai-sten.online/articles/people/vestyak_signature.gif",
            "https://mai-sten.online/prepods/Jeremy-Hillary-Boob-PhD_form-header.png",
            "https://mai-sten.online/prepods/08/vestyak/e_ln(x).png",
            "https://mai-sten.online/prepods/08/vestyak/BTR_Vestyak_fake.jpg",
            "https://mai-sten.online/prepods/08/vestyak/vestyak_sheet_small.jpg",
            "http://stat.livejournal.com/img/community.gif",
            "https://mai-sten.online/prepods/08/osokin/notes.zip",
            "not a url",
        ] {
            assert!(!is_teacher_photo_url(url), "{url}");
        }
    }

    #[test]
    fn derives_large_photo_from_small_name() {
        let page = r#"
            <img src="/prepods/08/osokin/prepods_804_osokin_small.jpg">
            <a href="/prepods/08/osokin/prepods_804_osokin2.jpg">
                <img src="/prepods/08/osokin/prepods_804_osokin2_small.jpg">
            </a>
        "#;

        let photos = parse_large_photos(page, "https://mai-sten.online/prepods/08/osokin/");

        assert_eq!(
            photos
                .get("https://mai-sten.online/prepods/08/osokin/prepods_804_osokin_small.jpg")
                .map(String::as_str),
            Some("https://mai-sten.online/prepods/08/osokin/prepods_804_osokin.jpg")
        );
        assert_eq!(
            photos
                .get("https://mai-sten.online/prepods/08/osokin/prepods_804_osokin2_small.jpg")
                .map(String::as_str),
            Some("https://mai-sten.online/prepods/08/osokin/prepods_804_osokin2.jpg")
        );
    }

    #[test]
    fn ignores_foreign_domains() {
        assert!(!is_teacher_photo_url(
            "https://example.com/prepods/08/osokin/prepods_804_osokin_small.jpg"
        ));
        assert!(!is_teacher_photo_url(
            "http://mai-sten.online/prepods/08/osokin/prepods_804_osokin_small.jpg"
        ));
    }

    #[test]
    fn derives_large_photo_ignoring_query() {
        assert_eq!(
            full_size_photo_name(
                "https://mai-sten.online/prepods/08/osokin/prepods_804_osokin_small.jpg?v=2.1"
            )
            .as_deref(),
            Some("https://mai-sten.online/prepods/08/osokin/prepods_804_osokin.jpg?v=2.1")
        );
    }

    #[test]
    fn parse_photos_keeps_only_teacher_images() {
        let page = r#"
            <img src="/images/download_icon.gif">
            <img src="/images/file_type_MP3.gif">
            <img src="/images/Telegram_logo_2019_inverted.gif">
            <img src="http://stat.livejournal.com/img/community.gif">
            <img src="/articles/people/vestyak_signature.gif">
            <a href="/prepods/08/osokin/prepods_804_osokin.jpg">
                <img src="/prepods/08/osokin/prepods_804_osokin_small.jpg">
            </a>
            <a href="/prepods/08/osokin/prepods_804_osokin2.jpg">
                <img src="prepods_804_osokin2_small.jpg">
            </a>
            <img src="/prepods/08/osokin/e_ln(x).png">
            <img src="/prepods/08/osokin/notes.zip">
        "#;

        let photos = parse_photos(page, "https://mai-sten.online/prepods/08/osokin/");

        assert_eq!(
            photos,
            vec![
                "https://mai-sten.online/prepods/08/osokin/prepods_804_osokin_small.jpg".to_owned(),
                "https://mai-sten.online/prepods/08/osokin/prepods_804_osokin2_small.jpg".to_owned(),
            ]
        );
    }
}
