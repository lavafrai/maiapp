use std::sync::Arc;

use axum::{
    Json, Router,
    extract::{Path, Query, State},
    http::{HeaderMap, HeaderValue, StatusCode, header},
    response::{IntoResponse, Response}, routing::get,
};
use serde::Deserialize;

use super::{domain::Source, service::PublicationService};
use crate::state::AppState;

pub fn router() -> Router<AppState> {
    Router::new()
        .route("/exports", get(formats))
        .route("/exports/{kind}/{id}/{format}", get(export))
}

async fn formats(State(state): State<AppState>) -> impl IntoResponse {
    Json(state.publications.formats())
}

#[derive(Default, Deserialize)]
pub struct ExportQuery {
    #[serde(default)]
    pub download: bool,
}

async fn export(
    State(state): State<AppState>,
    Path((kind, id, format)): Path<(String, String, String)>,
    Query(query): Query<ExportQuery>, headers: HeaderMap,
) -> Response {
    state.telemetry.record_endpoint("/exports/{kind}/{id}/{format}").await;
    serve(&state.publications, &kind, &id, &format, query, headers).await
}

pub(super) async fn serve(
    service: &Arc<PublicationService>, kind: &str, id: &str, format: &str,
    query: ExportQuery, headers: HeaderMap,
) -> Response {
    // Axum has already decoded path segments; never decode them twice.
    let source = match Source::parse(kind, id) {
        Ok(source) => source,
        Err(_) => return failure(StatusCode::BAD_REQUEST, "Некорректный идентификатор расписания."),
    };
    let Some(exporter) = service.registry.get(format) else {
        return failure(StatusCode::NOT_FOUND, "Этот формат экспорта не поддерживается.");
    };
    let output = match service.get(source.clone(), format).await {
        Ok(output) => output,
        Err(error) => {
            tracing::warn!(source = %source.key(), %error, "publication unavailable");
            return failure(StatusCode::SERVICE_UNAVAILABLE, "Расписание временно недоступно. Повторите запрос позднее.");
        }
    };
    let not_modified = headers.get(header::IF_NONE_MATCH).and_then(|v| v.to_str().ok())
        .is_some_and(|v| etag_matches(v, &output.artifact.etag));
    let mut response = if not_modified {
        StatusCode::NOT_MODIFIED.into_response()
    } else {
        output.artifact.bytes.into_response()
    };
    let result_headers = response.headers_mut();
    result_headers.insert(header::CONTENT_TYPE, HeaderValue::from_static(exporter.format().mime_type));
    result_headers.insert(header::CACHE_CONTROL, HeaderValue::from_static("public, max-age=300, must-revalidate"));
    result_headers.insert(header::X_CONTENT_TYPE_OPTIONS, HeaderValue::from_static("nosniff"));
    insert_header(result_headers, header::ETAG, &output.artifact.etag);
    if let Some(date) = chrono::DateTime::from_timestamp(output.artifact.modified_at, 0) {
        insert_header(result_headers, header::LAST_MODIFIED, &date.format("%a, %d %b %Y %H:%M:%S GMT").to_string());
    }
    insert_header(result_headers, axum::http::HeaderName::from_static("x-maiapp-source-observed-at"),
        &output.source_observed_at.to_string());
    result_headers.insert("x-maiapp-publication-state", HeaderValue::from_static(
        if output.held { "held" } else if output.stale { "stale" } else { "current" }
    ));
    if let Some(filename) = service.filename(&source, format) {
        insert_header(result_headers, header::CONTENT_DISPOSITION, &format!("{}; filename=\"{}\"",
            if query.download { "attachment" } else { "inline" }, filename));
    }
    response
}

fn insert_header(headers: &mut HeaderMap, name: axum::http::HeaderName, value: &str) {
    if let Ok(value) = HeaderValue::from_str(value) { headers.insert(name, value); }
}

fn failure(status: StatusCode, message: &str) -> Response {
    let mut response = (status, Json(serde_json::json!({"error": message}))).into_response();
    response.headers_mut().insert(header::CACHE_CONTROL, HeaderValue::from_static("no-store"));
    if status == StatusCode::SERVICE_UNAVAILABLE {
        response.headers_mut().insert(header::RETRY_AFTER, HeaderValue::from_static("60"));
    }
    response
}

pub(super) fn etag_matches(value: &str, etag: &str) -> bool {
    value.split(',').map(str::trim).any(|tag| tag == "*" || tag.strip_prefix("W/").unwrap_or(tag) == etag)
}
