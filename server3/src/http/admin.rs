use axum::{
    Json, Router,
    extract::State,
    http::{HeaderMap, HeaderValue, StatusCode, header},
    response::{Html, IntoResponse, Response},
    routing::get,
};
use base64::{Engine, engine::general_purpose};

use crate::{config::AppConfig, state::AppState, telemetry::TelemetrySnapshot};

pub fn router(config: AppConfig) -> Router<AppState> {
    Router::new()
        .route(
            "/admin",
            get({
                let config = config.clone();
                move |state, headers| admin_page(state, headers, config.clone())
            }),
        )
        .route(
            "/admin/stats.json",
            get(move |state, headers| admin_stats(state, headers, config.clone())),
        )
}

async fn admin_page(
    State(state): State<AppState>,
    headers: HeaderMap,
    config: AppConfig,
) -> Response {
    if let Err(response) = authorize(&headers, &config) {
        return response;
    }
    let snapshot = state.telemetry.snapshot().await;
    Html(render_admin(snapshot)).into_response()
}

async fn admin_stats(
    State(state): State<AppState>,
    headers: HeaderMap,
    config: AppConfig,
) -> Response {
    if let Err(response) = authorize(&headers, &config) {
        return response;
    }
    Json(state.telemetry.snapshot().await).into_response()
}

fn authorize(headers: &HeaderMap, config: &AppConfig) -> Result<(), Response> {
    let Some(header) = headers
        .get(header::AUTHORIZATION)
        .and_then(|value| value.to_str().ok())
    else {
        return Err(unauthorized());
    };
    let Some(encoded) = header.strip_prefix("Basic ") else {
        return Err(unauthorized());
    };
    let Ok(decoded) = general_purpose::STANDARD.decode(encoded) else {
        return Err(unauthorized());
    };
    let Ok(credentials) = String::from_utf8(decoded) else {
        return Err(unauthorized());
    };
    let expected = format!("{}:{}", config.admin_username, config.admin_password);
    if credentials == expected {
        Ok(())
    } else {
        Err(unauthorized())
    }
}

fn unauthorized() -> Response {
    let mut response = (StatusCode::UNAUTHORIZED, "Unauthorized").into_response();
    response.headers_mut().insert(
        header::WWW_AUTHENTICATE,
        HeaderValue::from_static(r#"Basic realm="MAIapp server3 admin", charset="UTF-8""#),
    );
    response
}

fn render_admin(snapshot: TelemetrySnapshot) -> String {
    let upstream_cards = snapshot
        .upstreams
        .iter()
        .map(|upstream| {
            let status = upstream
                .current_ok
                .map(|ok| if ok { "up" } else { "down" })
                .unwrap_or("unknown");
            let latest = upstream
                .current_latency_ms
                .map(|latency_ms| {
                    format!(
                        "{} ms, status {}",
                        latency_ms,
                        upstream
                            .current_status
                            .map(|s| s.to_string())
                            .unwrap_or_else(|| "-".to_owned())
                    )
                })
                .unwrap_or_else(|| "no probes yet".to_owned());
            let bars = upstream
                .timeline
                .iter()
                .rev()
                .take(120)
                .collect::<Vec<_>>()
                .into_iter()
                .rev()
                .map(|probe| {
                    let class = if probe.ok { "ok" } else { "fail" };
                    format!(r#"<span class="bar {class}" title="{}: {} ms"></span>"#, probe.checked_at, probe.latency_ms)
                })
                .collect::<String>();
            format!(
                r#"<section class="card"><h2>{}</h2><div class="metric {}">{}</div><p>Current: {}</p><p>Month uptime: {:.2}% from {} probes</p><div class="timeline">{}</div></section>"#,
                escape(&upstream.name),
                status,
                status,
                escape(&latest),
                upstream.uptime_percent_month,
                upstream.probes_month,
                bars
            )
        })
        .collect::<String>();

    format!(
        r#"<!doctype html>
<html lang="ru">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>MAIapp server3 admin</title>
<style>
:root {{ color-scheme: dark; font-family: Inter, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif; background: #101114; color: #f2f2f3; }}
body {{ margin: 0; }}
main {{ max-width: 1320px; margin: 0 auto; padding: 28px; }}
header {{ display: flex; justify-content: space-between; gap: 20px; align-items: end; margin-bottom: 24px; }}
h1 {{ font-size: 30px; margin: 0 0 6px; }}
h2 {{ font-size: 16px; margin: 0 0 14px; color: #cfd0d6; }}
p {{ margin: 0; color: #a7a9b3; }}
.grid {{ display: grid; grid-template-columns: repeat(auto-fit, minmax(260px, 1fr)); gap: 14px; margin-bottom: 18px; }}
.card {{ background: #1a1c22; border: 1px solid #30333d; border-radius: 8px; padding: 18px; }}
.metric {{ font-size: 34px; font-weight: 760; margin-bottom: 8px; }}
.toolbar {{ display: flex; flex-wrap: wrap; gap: 10px; margin-bottom: 12px; }}
input, select {{ background: #101114; color: #f2f2f3; border: 1px solid #30333d; border-radius: 6px; padding: 9px 10px; }}
input {{ min-width: 220px; flex: 1; }}
.up {{ color: #77d68b; }}
.down, .fail {{ color: #ff7a7a; }}
.unknown {{ color: #d8c16d; }}
table {{ width: 100%; border-collapse: collapse; font-size: 14px; }}
th, td {{ padding: 9px 0; border-bottom: 1px solid #30333d; text-align: left; }}
th {{ color: #a7a9b3; font-weight: 600; }}
td:last-child, th:last-child {{ text-align: right; }}
.timeline {{ display: flex; gap: 2px; min-height: 36px; align-items: end; margin-top: 14px; overflow: hidden; }}
.bar {{ display: inline-block; width: 5px; height: 28px; border-radius: 2px; background: #77d68b; }}
.bar.fail {{ background: #ff7a7a; }}
.muted {{ color: #a7a9b3; }}
.wide {{ grid-column: 1 / -1; }}
a {{ color: #8db7ff; }}
</style>
</head>
<body>
<main>
<header>
  <div><h1>MAIapp server3</h1><p>Admin dashboard</p></div>
  <div class="muted">Started: {}<br>Process uptime: {} seconds<br>Rolling window: {} days<br>Total requests: {}</div>
</header>
<div class="grid">
  <section class="card"><h2>Endpoint requests</h2>{}</section>
  <section class="card"><h2>Popular groups</h2><div id="groups"></div></section>
  <section class="card"><h2>Popular schedules</h2><div id="schedules"></div></section>
  <section class="card"><h2>Exler review requests</h2><div id="reviews"></div></section>
</div>
<div class="grid">
  <section class="card"><h2>Cache workers</h2>
    <div class="metric"><span id="cache-active">0</span> active / <span id="cache-queued">0</span> queued</div>
    <p>Month max: <span id="cache-max"></span></p>
  </section>
  <section class="card"><h2>Cache worker errors</h2><div id="cache-errors"></div></section>
  <section class="card"><h2>Cache worker timeouts</h2><div id="cache-timeouts"></div></section>
  <section class="card"><h2>Cache waiters</h2><div id="cache-waiters"></div></section>
  <section class="card wide"><h2>Active cache workers</h2><div id="active-workers"></div></section>
</div>
<div class="grid">{}</div>
<section class="card wide">
  <h2>Large data browser</h2>
  <div class="toolbar">
    <select id="dataset">
      <option value="popular_groups">Groups</option>
      <option value="popular_schedules">Schedules</option>
      <option value="popular_exler_reviews">Exler reviews</option>
      <option value="endpoint_requests">Endpoints</option>
      <option value="cache_errors">Cache errors</option>
      <option value="cache_timeouts">Cache timeouts</option>
      <option value="cache_waiters">Cache waiters</option>
    </select>
    <input id="search" placeholder="Search">
    <select id="limit">
      <option>50</option>
      <option selected>100</option>
      <option>250</option>
      <option>500</option>
    </select>
  </div>
  <div id="browser"></div>
</section>
<p class="muted">JSON: <a href="/admin/stats.json">/admin/stats.json</a></p>
</main>
<script>
const stats = {{}};
function table(entries) {{
  if (!entries.length) return '<p class="muted">No data yet</p>';
  return '<table><thead><tr><th>Name</th><th>Requests</th></tr></thead><tbody>' +
    entries.map(e => `<tr><td>${{escapeHtml(e.key)}}</td><td>${{e.count}}</td></tr>`).join('') +
    '</tbody></table>';
}}
function duration(ms) {{
  const seconds = Math.floor(Number(ms || 0) / 1000);
  const minutes = Math.floor(seconds / 60);
  const hours = Math.floor(minutes / 60);
  if (hours) return `${{hours}}h ${{minutes % 60}}m ${{seconds % 60}}s`;
  if (minutes) return `${{minutes}}m ${{seconds % 60}}s`;
  return `${{seconds}}s`;
}}
function activeWorkerTable(workers) {{
  if (!workers.length) return '<p class="muted">No active workers</p>';
  return '<table><thead><tr><th>Namespace</th><th>Key</th><th>Running</th><th>Started</th><th>Wait</th></tr></thead><tbody>' +
    workers.map(w => `<tr><td>${{escapeHtml(w.namespace)}}</td><td>${{escapeHtml(w.key)}}</td><td>${{duration(w.running_ms)}}</td><td>${{new Date(w.started_at * 1000).toLocaleString()}}</td><td>${{duration(w.wait_ms)}}</td></tr>`).join('') +
    '</tbody></table>';
}}
function escapeHtml(value) {{
  return String(value).replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('>', '&gt;').replaceAll('"', '&quot;');
}}
function renderBrowser() {{
  const dataset = document.getElementById('dataset').value;
  const query = document.getElementById('search').value.toLowerCase();
  const limit = Number(document.getElementById('limit').value);
  const entries = (stats[dataset] || []).filter(e => e.key.toLowerCase().includes(query)).slice(0, limit);
  document.getElementById('browser').innerHTML = table(entries);
}}
Object.assign(stats, __STATS__);
stats.cache_errors = stats.cache_workers.errors || [];
stats.cache_timeouts = stats.cache_workers.timeouts || [];
stats.cache_waiters = stats.cache_workers.waiters || [];
document.getElementById('groups').innerHTML = table(stats.popular_groups.slice(0, 10));
document.getElementById('schedules').innerHTML = table(stats.popular_schedules.slice(0, 10));
document.getElementById('reviews').innerHTML = table(stats.popular_exler_reviews.slice(0, 10));
document.getElementById('cache-active').textContent = stats.cache_workers.active;
document.getElementById('cache-queued').textContent = stats.cache_workers.queued;
document.getElementById('cache-max').textContent = `${{stats.cache_workers.max_active_month}} active / ${{stats.cache_workers.max_queued_month}} queued`;
document.getElementById('cache-errors').innerHTML = table(stats.cache_errors);
document.getElementById('cache-timeouts').innerHTML = table(stats.cache_timeouts);
document.getElementById('cache-waiters').innerHTML = table(stats.cache_waiters);
document.getElementById('active-workers').innerHTML = activeWorkerTable(stats.cache_workers.active_workers || []);
['dataset', 'search', 'limit'].forEach(id => document.getElementById(id).addEventListener('input', renderBrowser));
renderBrowser();
setInterval(() => location.reload(), 30000);
</script>
</body>
</html>"#,
        escape(&snapshot.started_at),
        snapshot.uptime_seconds,
        snapshot.retention_days,
        snapshot.total_requests,
        count_table(&snapshot.endpoint_requests),
        upstream_cards
    )
    .replace("__STATS__", &serde_json::to_string(&snapshot).unwrap_or_else(|_| "{}".to_owned()))
}

fn count_table(entries: &[crate::telemetry::CountEntry]) -> String {
    if entries.is_empty() {
        return r#"<p class="muted">No data yet</p>"#.to_owned();
    }
    let rows = entries
        .iter()
        .map(|entry| {
            format!(
                "<tr><td>{}</td><td>{}</td></tr>",
                escape(&entry.key),
                entry.count
            )
        })
        .collect::<String>();
    format!(
        "<table><thead><tr><th>Name</th><th>Requests</th></tr></thead><tbody>{rows}</tbody></table>"
    )
}

fn escape(value: &str) -> String {
    value
        .replace('&', "&amp;")
        .replace('<', "&lt;")
        .replace('>', "&gt;")
        .replace('"', "&quot;")
}
