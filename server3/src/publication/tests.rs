use std::{collections::{BTreeSet, VecDeque}, path::PathBuf, sync::{Arc, Mutex, atomic::{AtomicU64, AtomicUsize, Ordering}}, time::Duration};

use axum::{body::to_bytes, http::{HeaderMap, HeaderValue, StatusCode, header}};
use futures::future::BoxFuture;

use super::{
    domain::{self, PublicationState, Source, normalize, reconcile},
    exporters::{ExportArtifact, ExportFormat, ScheduleExporter, ical::{IcalendarExporter, content_line, escape_text}},
    http::{self, ExportQuery},
    service::{PublicationConfig, PublicationService, ScheduleProvider},
    storage::PublicationRecord,
};
use crate::models::{Group, Schedule};

const NOW: i64 = 1_788_782_400;

fn source() -> Source { Source::parse("group", "М4О-306Б-23").unwrap() }
fn fixture() -> Schedule {
    serde_json::from_str(include_str!("../../tests/fixtures/publication/group.json")).unwrap()
}
fn state(schedule: Schedule) -> PublicationState {
    reconcile(None, normalize(source(), schedule).unwrap(), NOW, 900).unwrap()
}
fn updated(previous: &PublicationState, schedule: Schedule, now: i64) -> PublicationState {
    reconcile(Some(previous), normalize(source(), schedule).unwrap(), now, 900).unwrap()
}
fn ics(state: &PublicationState) -> String {
    String::from_utf8(IcalendarExporter.render(&state.snapshot).unwrap()).unwrap()
}
fn ids(state: &PublicationState) -> BTreeSet<String> {
    state.snapshot.occurrences.iter().map(|o| o.uid.clone()).collect()
}

#[test]
fn canonical_sources_reject_paths_names_and_unknown_kinds() {
    assert!(Source::parse("group", "М4О-306Б-23").is_ok());
    assert!(Source::parse("teacher", "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee").is_ok());
    for (kind, id) in [("group", "../foo"), ("group", "%D0%9C4О-306Б-23"),
        ("teacher", "Иванов Иван Иванович"), ("local", "x"), ("group", "М4О-306Б-23\r\n")] {
        assert!(Source::parse(kind, id).is_err());
    }
}

#[test]
fn normalization_preserves_occurrences_and_explicit_coverage() {
    let candidate = normalize(source(), fixture()).unwrap();
    assert_eq!(candidate.occurrences.len(), 3);
    assert!(!candidate.coverage.complete);
    assert_eq!(candidate.coverage.from.to_string(), "2026-09-07");
    assert_eq!(candidate.coverage.through.to_string(), "2026-09-08");
    assert_eq!(candidate.occurrences[0].groups, vec!["М4О-306Б-23"]);
}

#[test]
fn normalization_rejects_empty_mismatched_and_partial_invalid_data() {
    let mut schedule = fixture();
    schedule.days.clear();
    assert!(normalize(source(), schedule).is_err());
    let mut schedule = fixture();
    schedule.name = "М4О-307Б-23".into();
    assert!(normalize(source(), schedule).is_err());
    let mut schedule = fixture();
    schedule.days[0].lessons[1].day = schedule.days[1].date;
    assert!(normalize(source(), schedule).is_err());
    for invalid in ["25:00", "10:30:99", "23:59:60", "", "09:00\r\nSTATUS:CANCELLED"] {
        let mut schedule = fixture();
        schedule.days[0].lessons[0].time_start.time = invalid.into();
        assert!(normalize(source(), schedule).is_err(), "accepted {invalid:?}");
    }
    let mut schedule = fixture();
    schedule.days[0].lessons[0].time_end.time = "09:00:00".into();
    assert!(normalize(source(), schedule).is_err());
    let mut schedule = fixture();
    schedule.days.push(schedule.days[0].clone());
    assert!(normalize(source(), schedule).is_err());
}

#[test]
fn teacher_adapter_separates_groups_from_lectors() {
    let mut schedule = fixture();
    schedule.name = "Тестовый Преподаватель".into();
    for day in &mut schedule.days {
        for lesson in &mut day.lessons {
            lesson.lectors[0].name.name = "М4О-306Б-23".into();
            lesson.lectors[0].uid.uid = "М4О-306Б-23".into();
        }
    }
    let teacher = Source::parse("teacher", "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee").unwrap();
    let candidate = normalize(teacher.clone(), schedule).unwrap();
    assert_eq!(candidate.occurrences[0].teachers[0].id, teacher.id);
    assert_eq!(candidate.occurrences[0].teachers[0].name, "Тестовый Преподаватель");
    assert_eq!(candidate.occurrences[0].groups, vec!["М4О-306Б-23"]);
}

#[test]
fn reordered_inputs_keep_uids_sequences_bytes_and_revision() {
    let first = state(fixture());
    let mut schedule = fixture();
    schedule.created += 1800;
    schedule.days.reverse();
    for day in &mut schedule.days { day.lessons.reverse(); }
    let second = updated(&first, schedule, NOW + 1800);
    assert_eq!(first.snapshot, second.snapshot);
    assert_eq!(ics(&first), ics(&second));
}

#[test]
fn identical_duplicates_preserve_multiplicity_and_identity() {
    let mut schedule = fixture();
    let duplicate = schedule.days[0].lessons[0].clone();
    schedule.days[0].lessons.push(duplicate);
    let first = state(schedule.clone());
    assert_eq!(first.snapshot.occurrences.len(), 4);
    assert_eq!(ids(&first).len(), 4);
    schedule.days[0].lessons.reverse();
    schedule.created += 1800;
    let second = updated(&first, schedule, NOW + 1800);
    assert_eq!(ids(&first), ids(&second));
    assert_eq!(ics(&first), ics(&second));
}

#[test]
fn metadata_update_preserves_identity_and_changes_only_one_sequence() {
    let first = state(fixture());
    let mut schedule = fixture();
    schedule.created += 1800;
    schedule.days[0].lessons[0].rooms[0].name = "ГАК-999".into();
    schedule.days[0].lessons[0].name = "Исправленное название".into();
    let second = updated(&first, schedule, NOW + 1800);
    assert_eq!(ids(&first), ids(&second));
    assert_eq!(second.snapshot.occurrences.iter().filter(|o| o.sequence == 1).count(), 1);
    assert_ne!(ics(&first), ics(&second));
}

#[test]
fn unambiguous_same_day_time_move_keeps_uid() {
    let first = state(fixture());
    let mut schedule = fixture();
    schedule.created += 1800;
    schedule.days[0].lessons[0].time_start.time = "08:30".into();
    let second = updated(&first, schedule, NOW + 1800);
    assert_eq!(ids(&first), ids(&second));
    assert_eq!(second.snapshot.occurrences[0].sequence, 1);
}

#[test]
fn cancellation_requires_independent_observations_and_survives_restart() {
    let first = state(fixture());
    let mut schedule = fixture();
    schedule.days[0].lessons.remove(1);
    schedule.created += 1800;
    let pending = updated(&first, schedule.clone(), NOW + 1800);
    assert!(pending.pending.is_some());
    assert_eq!(ics(&first), ics(&pending));
    let repeated_cache = updated(&pending, schedule.clone(), NOW + 3600);
    assert!(repeated_cache.pending.is_some());
    let restarted: PublicationState = serde_json::from_slice(&serde_json::to_vec(&repeated_cache).unwrap()).unwrap();
    schedule.created += 900;
    let confirmed = updated(&restarted, schedule, NOW + 3600);
    assert!(confirmed.pending.is_none());
    assert_eq!(confirmed.snapshot.occurrences.iter().filter(|o| o.cancelled).count(), 1);
    assert_eq!(ids(&first), ids(&confirmed));
    assert!(ics(&confirmed).contains("STATUS:CANCELLED\r\n"));
    let mut restored_schedule = fixture();
    restored_schedule.created += 5400;
    let restored = updated(&confirmed, restored_schedule, NOW + 5400);
    assert_eq!(ids(&first), ids(&restored));
    assert!(restored.snapshot.occurrences.iter().all(|o| !o.cancelled));
    assert_eq!(restored.snapshot.occurrences.iter().filter(|o| o.sequence == 2).count(), 1);
}

#[test]
fn mass_removal_threshold_is_strictly_above_eighty_percent() {
    for removed in [79, 80, 81] {
        let mut schedule = fixture();
        schedule.days.truncate(1);
        let lesson = schedule.days[0].lessons[0].clone();
        schedule.days[0].lessons = (0..100).map(|i| {
            let mut item = lesson.clone();
            item.name = format!("Lesson {i}");
            item
        }).collect();
        let first = state(schedule.clone());
        schedule.days[0].lessons.truncate(100 - removed);
        schedule.created += 1800;
        let result = reconcile(Some(&first), normalize(source(), schedule.clone()).unwrap(), NOW + 1800, 900);
        if removed > 80 {
            assert!(result.is_err(), "{removed}% must be rejected");
            schedule.created += 900;
            assert!(reconcile(Some(&first), normalize(source(), schedule).unwrap(), NOW + 2700, 900).is_err());
        } else {
            let pending = result.unwrap();
            assert!(pending.pending.is_some());
            assert_eq!(ics(&first), ics(&pending));
            schedule.created += 900;
            let confirmed = updated(&pending, schedule, NOW + 2700);
            assert!(confirmed.pending.is_none());
            assert_eq!(confirmed.snapshot.occurrences.iter().filter(|o| o.cancelled).count(), removed);
            assert_eq!(ids(&first), ids(&confirmed));
        }
    }
}

#[test]
fn missing_dates_outside_new_coverage_are_retained_not_cancelled() {
    let first = state(fixture());
    let mut schedule = fixture();
    schedule.days.remove(0);
    schedule.created += 1800;
    let second = updated(&first, schedule, NOW + 1800);
    assert_eq!(ids(&first), ids(&second));
    assert!(second.snapshot.occurrences.iter().all(|o| !o.cancelled));
    assert_eq!(second.snapshot.occurrences.iter().filter(|o| o.retained_outside_coverage).count(), 2);
}

#[test]
fn invalid_or_backwards_observation_cannot_replace_publication() {
    let first = state(fixture());
    for observed in [0, NOW - 1, NOW + 301] {
        let mut schedule = fixture();
        schedule.created = observed;
        assert!(reconcile(Some(&first), normalize(source(), schedule).unwrap(), NOW, 900).is_err());
    }
}

#[test]
fn ical_text_escaping_blocks_property_injection() {
    assert_eq!(escape_text("A\\B,C;D\r\nE\rF\nG"), "A\\\\B\\,C\\;D\\nE\\nF\\nG");
    let mut schedule = fixture();
    schedule.days[0].lessons[0].name = "Math\r\nEND:VEVENT\r\nBEGIN:VEVENT".into();
    let calendar = ics(&state(schedule));
    assert_eq!(calendar.matches("\r\nBEGIN:VEVENT\r\n").count(), 3);
    assert_eq!(calendar.matches("\r\nEND:VEVENT\r\n").count(), 3);
}

#[test]
fn line_folding_limits_utf8_octets_and_round_trips() {
    let logical = format!("SUMMARY:{}", "Календарь 🐈, математика; ".repeat(40));
    let mut physical = String::new();
    content_line(&mut physical, &logical);
    for line in physical.split("\r\n") { assert!(line.len() <= 75, "{}", line.len()); }
    assert_eq!(physical.replace("\r\n ", "").strip_suffix("\r\n"), Some(logical.as_str()));
    assert!(!physical.replace("\r\n", "").contains('\n'));
}

#[test]
fn ical_uses_utc_including_midnight_rollover_and_no_recurrence_guessing() {
    let mut schedule = fixture();
    schedule.days[0].lessons[0].time_start.time = "01:00:00".into();
    schedule.days[0].lessons[0].time_end.time = "02:00:00".into();
    let calendar = ics(&state(schedule));
    assert!(calendar.starts_with("BEGIN:VCALENDAR\r\nVERSION:2.0\r\n"));
    assert!(calendar.ends_with("END:VCALENDAR\r\n"));
    assert!(calendar.contains("DTSTART:20260906T220000Z\r\n"));
    assert!(calendar.contains("DTEND:20260906T230000Z\r\n"));
    assert!(!calendar.contains("RRULE"));
    assert!(!calendar.contains("BEGIN:VALARM"));
    assert!(!calendar.contains("METHOD:"));
}

#[test]
fn weak_list_and_wildcard_etags_work() {
    for header in ["\"abc\"", "W/\"abc\"", "\"other\", W/\"abc\"", "*"] {
        assert!(http::etag_matches(header, "\"abc\""));
    }
    assert!(!http::etag_matches("\"different\"", "\"abc\""));
}

struct TestDir(PathBuf);
impl TestDir {
    fn new() -> Self {
        static NEXT: AtomicU64 = AtomicU64::new(0);
        Self(std::env::temp_dir().join(format!("maiapp-publication-test-{}-{}-{}", std::process::id(),
            chrono::Utc::now().timestamp_nanos_opt().unwrap(), NEXT.fetch_add(1, Ordering::SeqCst))))
    }
}
impl Drop for TestDir { fn drop(&mut self) { let _ = std::fs::remove_dir_all(&self.0); } }

struct Provider {
    queue: Mutex<VecDeque<Result<Schedule, &'static str>>>,
    calls: AtomicUsize,
    delay: Duration,
}
impl Provider {
    fn new(values: Vec<Result<Schedule, &'static str>>) -> Arc<Self> {
        Arc::new(Self { queue: Mutex::new(values.into()), calls: AtomicUsize::new(0), delay: Duration::ZERO })
    }
}
impl ScheduleProvider for Provider {
    fn fetch<'a>(&'a self, _: &'a Source) -> BoxFuture<'a, anyhow::Result<Schedule>> {
        Box::pin(async move {
            self.calls.fetch_add(1, Ordering::SeqCst);
            tokio::time::sleep(self.delay).await;
            self.queue.lock().unwrap().pop_front().unwrap_or(Err("offline")).map_err(|e| anyhow::anyhow!(e))
        })
    }
}
fn config(dir: &TestDir) -> PublicationConfig {
    PublicationConfig { directory: dir.0.clone(), refresh_interval: Duration::from_secs(900),
        failure_backoff: Duration::from_secs(60), worker_timeout: Duration::from_secs(1), confirmation_seconds: 900 }
}
fn service(dir: &TestDir, provider: Arc<dyn ScheduleProvider>) -> Arc<PublicationService> {
    Arc::new(PublicationService::new(provider, config(dir)))
}
fn record(service: &PublicationService) -> PublicationRecord {
    let mut schedule = fixture();
    schedule.created = chrono::Utc::now().timestamp();
    service.prepare(None, source(), schedule, chrono::Utc::now().timestamp()).unwrap()
}

#[tokio::test]
async fn atomic_store_restores_exact_calendar_after_service_restart() {
    let dir = TestDir::new();
    let first = service(&dir, Provider::new(vec![]));
    let original = record(&first);
    first.store.save(&source(), &original).await.unwrap();
    drop(first);
    let restarted = service(&dir, Provider::new(vec![Err("network unavailable")]));
    let restored = restarted.get(source(), "ics").await.unwrap();
    assert_eq!(restored.artifact.bytes, original.artifacts["ics"].bytes);
    assert_eq!(restored.artifact.etag, original.artifacts["ics"].etag);
    let stored = restarted.store.load(&source()).await.unwrap().unwrap();
    assert_eq!(stored.state.snapshot, original.state.snapshot);
}

#[tokio::test]
async fn invalid_record_does_not_overwrite_last_good_and_corruption_is_not_reset() {
    let dir = TestDir::new();
    let service = service(&dir, Provider::new(vec![]));
    let original = record(&service);
    service.store.save(&source(), &original).await.unwrap();
    let mut invalid = original.clone();
    invalid.artifacts.get_mut("ics").unwrap().bytes.clear();
    assert!(service.store.save(&source(), &invalid).await.is_err());
    assert_eq!(service.store.load(&source()).await.unwrap().unwrap().artifacts["ics"].bytes, original.artifacts["ics"].bytes);
    std::fs::write(service.store.path(&source()), b"corrupt").unwrap();
    assert!(service.get(source(), "ics").await.is_err());
    assert_eq!(std::fs::read(service.store.path(&source())).unwrap(), b"corrupt");
}

#[tokio::test]
async fn unknown_schema_and_duplicate_identity_are_rejected() {
    let dir = TestDir::new();
    let service = service(&dir, Provider::new(vec![]));
    let mut data = record(&service);
    data.schema_version = 99;
    assert!(data.validate(&source()).is_err());
    data.schema_version = domain::SCHEMA_VERSION;
    data.state.snapshot.occurrences[1].uid = data.state.snapshot.occurrences[0].uid.clone();
    assert!(data.validate(&source()).is_err());
}

#[tokio::test]
async fn upstream_failure_serves_existing_artifact_and_never_truncates_disk() {
    let dir = TestDir::new();
    let provider = Provider::new(vec![Err("upstream timeout")]);
    let service = service(&dir, provider.clone());
    let mut original = record(&service);
    original.state.last_checked_at -= 3600;
    original.state.source_observed_at -= 3600;
    service.store.save(&source(), &original).await.unwrap();
    let output = service.get(source(), "ics").await.unwrap();
    assert!(output.stale);
    assert_eq!(output.artifact.bytes, original.artifacts["ics"].bytes);
    tokio::time::timeout(Duration::from_secs(1), async {
        while provider.calls.load(Ordering::SeqCst) == 0 { tokio::task::yield_now().await; }
    }).await.unwrap();
    assert_eq!(service.store.load(&source()).await.unwrap().unwrap().artifacts["ics"].bytes, original.artifacts["ics"].bytes);
}

#[tokio::test]
async fn cold_failure_is_503_not_empty_calendar_and_has_retry_after() {
    let dir = TestDir::new();
    let service = service(&dir, Provider::new(vec![Err("no data")]));
    let response = http::serve(&service, "group", &source().id, "ics", ExportQuery::default(), HeaderMap::new()).await;
    assert_eq!(response.status(), StatusCode::SERVICE_UNAVAILABLE);
    assert_eq!(response.headers()[header::RETRY_AFTER], "60");
    assert_eq!(response.headers()[header::CACHE_CONTROL], "no-store");
    assert!(response.headers()[header::CONTENT_TYPE].to_str().unwrap().starts_with("application/json"));
    assert!(!service.store.path(&source()).exists());
}

#[tokio::test]
async fn http_supports_download_304_and_invalid_request_errors() {
    let dir = TestDir::new();
    let service = service(&dir, Provider::new(vec![]));
    let original = record(&service);
    service.store.save(&source(), &original).await.unwrap();
    let response = http::serve(&service, "group", &source().id, "ics", ExportQuery { download: true }, HeaderMap::new()).await;
    assert_eq!(response.status(), StatusCode::OK);
    assert_eq!(response.headers()[header::CONTENT_TYPE], "text/calendar; charset=utf-8");
    assert!(response.headers()[header::CONTENT_DISPOSITION].to_str().unwrap().starts_with("attachment;"));
    assert_eq!(to_bytes(response.into_body(), 8 * 1024 * 1024).await.unwrap().as_ref(), original.artifacts["ics"].bytes);
    let mut headers = HeaderMap::new();
    headers.insert(header::IF_NONE_MATCH, HeaderValue::from_str(&format!("W/{}", original.artifacts["ics"].etag)).unwrap());
    let response = http::serve(&service, "group", &source().id, "ics", ExportQuery::default(), headers).await;
    assert_eq!(response.status(), StatusCode::NOT_MODIFIED);
    assert!(to_bytes(response.into_body(), 1024).await.unwrap().is_empty());
    for (id, format, status) in [("../x", "ics", StatusCode::BAD_REQUEST), ("М4О-306Б-23", "pdf", StatusCode::NOT_FOUND)] {
        assert_eq!(http::serve(&service, "group", id, format, ExportQuery::default(), HeaderMap::new()).await.status(), status);
    }
}

#[tokio::test]
async fn concurrent_cold_requests_use_one_source_fetch() {
    let dir = TestDir::new();
    let mut schedule = fixture();
    schedule.created = chrono::Utc::now().timestamp();
    let provider = Arc::new(Provider { queue: Mutex::new(vec![Ok(schedule)].into()), calls: AtomicUsize::new(0), delay: Duration::from_millis(80) });
    let service = service(&dir, provider.clone());
    let (a, b) = tokio::join!(service.get(source(), "ics"), service.get(source(), "ics"));
    assert!(a.is_ok() || b.is_ok());
    assert_eq!(provider.calls.load(Ordering::SeqCst), 1);
    assert!(service.store.load(&source()).await.unwrap().is_some());
}

struct FailingExporter;
impl ScheduleExporter for FailingExporter {
    fn format(&self) -> ExportFormat {
        ExportFormat { id: "future", title: "Test", description: "Failure isolation test", extension: "bin",
            mime_type: "application/octet-stream", subscription: false }
    }
    fn render(&self, _: &domain::ScheduleSnapshot) -> anyhow::Result<Vec<u8>> { anyhow::bail!("render failure") }
}

#[test]
fn one_failed_exporter_does_not_block_ics_or_replace_its_last_good_artifact() {
    let dir = TestDir::new();
    let mut service = PublicationService::new(Provider::new(vec![]), config(&dir));
    service.registry.register(Box::new(FailingExporter)).unwrap();
    assert!(service.registry.register(Box::new(FailingExporter)).is_err());
    let mut first = service.prepare(None, source(), fixture(), NOW).unwrap();
    let saved = ExportArtifact { bytes: b"older format".to_vec(), etag: format!("\"{}\"", domain::digest(b"older format")),
        modified_at: NOW, snapshot_revision: 1 };
    first.artifacts.insert("future".into(), saved.clone());
    let mut schedule = fixture();
    schedule.created += 1800;
    schedule.days[0].lessons[0].rooms[0].name = "Changed".into();
    let next = service.prepare(Some(&first), source(), schedule, NOW + 1800).unwrap();
    assert_eq!(next.artifacts["future"].bytes, saved.bytes);
    assert!(next.artifacts["future"].snapshot_revision < next.state.snapshot.revision);
    assert_ne!(next.artifacts["ics"].bytes, first.artifacts["ics"].bytes);
}

/// Run explicitly; network availability must never affect deterministic CI.
#[tokio::test]
#[ignore = "requires live public MAIapp API; run explicitly with --ignored"]
async fn live_public_api_smoke() {
    let base = "https://maiapp.lavafrai.ru/api/v1";
    let client = reqwest::Client::builder().timeout(Duration::from_secs(30)).build().unwrap();
    let groups: Vec<Group> = client.get(format!("{base}/groups")).send().await.unwrap()
        .error_for_status().unwrap().json().await.unwrap();
    let mut checked = 0;
    for group in groups.into_iter().filter(|g| Source::parse("group", &g.name).is_ok()).take(3) {
        let schedule: Schedule = client.get(format!("{base}/schedule/{}", urlencoding::encode(&group.name)))
            .send().await.unwrap().error_for_status().unwrap().json().await.unwrap();
        let candidate = normalize(Source::parse("group", &group.name).unwrap(), schedule).unwrap();
        let expected = candidate.occurrences.len();
        let state = reconcile(None, candidate, chrono::Utc::now().timestamp(), 900).unwrap();
        assert_eq!(ics(&state).matches("\r\nBEGIN:VEVENT\r\n").count(), expected);
        checked += 1;
    }
    assert_eq!(checked, 3, "need three real schedule fixtures");
}

#[tokio::test]
async fn disconnected_cold_request_does_not_cancel_publication_commit() {
    let dir = TestDir::new();
    let mut schedule = fixture();
    schedule.created = chrono::Utc::now().timestamp();
    let provider = Arc::new(Provider { queue: Mutex::new(vec![Ok(schedule)].into()), calls: AtomicUsize::new(0), delay: Duration::from_millis(80) });
    let service = service(&dir, provider.clone());
    let request_service = service.clone();
    let request = tokio::spawn(async move { request_service.get(source(), "ics").await });
    tokio::time::timeout(Duration::from_secs(2), async {
        while provider.calls.load(Ordering::SeqCst) == 0 { tokio::task::yield_now().await; }
    }).await.unwrap();
    request.abort();
    tokio::time::timeout(Duration::from_secs(2), async {
        while !service.store.path(&source()).exists() {
            tokio::time::sleep(Duration::from_millis(10)).await;
        }
    }).await.unwrap();
    assert!(service.store.load(&source()).await.unwrap().is_some());
    assert_eq!(provider.calls.load(Ordering::SeqCst), 1);
}

#[tokio::test]
async fn real_router_head_has_get_headers_and_no_body() {
    use crate::{config::AppConfig, repositories::{mai::MaiRepository, exler::ExlerRepository},
        services::{schedule::ScheduleService, exler::ExlerService, data::MaiDataService}, state::AppState, telemetry::Telemetry};
    let dir = TestDir::new();
    let publications = service(&dir, Provider::new(vec![]));
    publications.store.save(&source(), &record(&publications)).await.unwrap();
    let mut config = AppConfig::from_env();
    config.cache_dir = dir.0.join("cache");
    config.telemetry_path = dir.0.join("telemetry.json");
    let client = config.http_client().unwrap();
    let telemetry = Arc::new(Telemetry::load_or_new(&config).await);
    let mai = Arc::new(MaiRepository::new(client.clone(), &config, telemetry.clone()));
    let exler = Arc::new(ExlerRepository::new(client, &config, telemetry.clone()));
    let state = AppState::new(Arc::new(ScheduleService::new(mai)), Arc::new(ExlerService::new(exler)),
        Arc::new(MaiDataService::new()), telemetry, publications);
    let app = crate::http::router(config, state).unwrap();
    let listener = tokio::net::TcpListener::bind("127.0.0.1:0").await.unwrap();
    let address = listener.local_addr().unwrap();
    let server = tokio::spawn(async move { axum::serve(listener, app).await.unwrap() });
    let client = reqwest::Client::builder().timeout(Duration::from_secs(2)).build().unwrap();
    let url = format!("http://{address}/exports/group/{}/ics", urlencoding::encode(&source().id));
    let head = client.head(&url).send().await.unwrap();
    let get = client.get(&url).send().await.unwrap();
    assert_eq!(head.status(), StatusCode::OK);
    assert_eq!(head.headers()[header::ETAG], get.headers()[header::ETAG]);
    assert_eq!(head.headers()[header::CONTENT_TYPE], get.headers()[header::CONTENT_TYPE]);
    assert!(head.bytes().await.unwrap().is_empty());
    assert!(get.text().await.unwrap().starts_with("BEGIN:VCALENDAR\r\n"));
    let formats: serde_json::Value = client.get(format!("http://{address}/exports")).send().await.unwrap().json().await.unwrap();
    assert_eq!(formats[0]["id"], "ics");
    server.abort();
    let _ = server.await;
}
