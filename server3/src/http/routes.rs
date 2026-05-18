use axum::{
    Json, Router,
    extract::{Path, State},
    response::IntoResponse,
    routing::get,
};

use crate::{errors::AppResult, state::AppState};

use super::response::{cache, cache_with_status};

pub fn router() -> Router<AppState> {
    Router::new()
        .route("/", get(root))
        .route("/health", get(health))
        .route("/groups", get(groups))
        .route("/teachers", get(teachers))
        .route("/schedule/{id}", get(schedule))
        .route("/exler-teachers", get(exler_teachers))
        .route("/exler-teacher/{id}", get(exler_teacher))
        .route("/data", get(data))
}

async fn root() -> &'static str {
    "Hello, world!\nIt's MAI app server!"
}

async fn health() -> &'static str {
    "ok"
}

async fn groups(State(state): State<AppState>) -> AppResult<impl IntoResponse> {
    state.telemetry.record_endpoint("/groups").await;
    let groups = state.schedule.groups().await?;
    Ok(cache_with_status(
        Json(groups.value),
        1800,
        Some(groups.status),
    ))
}

async fn teachers(State(state): State<AppState>) -> impl IntoResponse {
    state.telemetry.record_endpoint("/teachers").await;
    cache(Json(state.schedule.teachers().await), 1800)
}

async fn schedule(
    State(state): State<AppState>,
    Path(id): Path<String>,
) -> AppResult<impl IntoResponse> {
    let id = decode_path_segment(&id);
    state.telemetry.record_schedule_request(&id).await;
    let schedule = state.schedule.schedule(&id).await?;
    Ok(cache_with_status(
        Json(schedule.value),
        3600,
        Some(schedule.status),
    ))
}

async fn exler_teachers(State(state): State<AppState>) -> AppResult<impl IntoResponse> {
    state.telemetry.record_endpoint("/exler-teachers").await;
    let teachers = state.exler.teachers().await?;
    Ok(cache_with_status(
        Json(teachers.value),
        3600,
        Some(teachers.status),
    ))
}

async fn exler_teacher(
    State(state): State<AppState>,
    Path(id): Path<String>,
) -> AppResult<impl IntoResponse> {
    let id = decode_path_segment(&id);
    state.telemetry.record_exler_review_request(&id).await;
    let teacher = state.exler.teacher_info(&id).await?;
    Ok(cache_with_status(
        Json(teacher.value),
        3600,
        Some(teacher.status),
    ))
}

async fn data(State(state): State<AppState>) -> impl IntoResponse {
    state.telemetry.record_endpoint("/data").await;
    cache(Json(state.data.manifest()), 3600)
}

fn decode_path_segment(value: &str) -> String {
    urlencoding::decode(value)
        .unwrap_or_else(|_| value.into())
        .into_owned()
}
