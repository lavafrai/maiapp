use axum::{
    http::{HeaderName, HeaderValue, header},
    response::{IntoResponse, Response},
};

use crate::cache::CacheStatus;

pub fn cache<T: IntoResponse>(response: T, _seconds: u64) -> Response {
    cache_with_status(response, _seconds, None)
}

pub fn cache_with_status<T: IntoResponse>(
    response: T,
    _seconds: u64,
    status: Option<CacheStatus>,
) -> Response {
    let mut response = response.into_response();
    response
        .headers_mut()
        .insert(header::CACHE_CONTROL, HeaderValue::from_static("no-store"));
    if let Some(status) = status {
        response.headers_mut().insert(
            HeaderName::from_static("x-maiapp-cache"),
            HeaderValue::from_static(status.as_header()),
        );
        if matches!(status, CacheStatus::Stale) {
            response.headers_mut().insert(
                HeaderName::from_static("x-maiapp-cache-refresh"),
                HeaderValue::from_static("started"),
            );
        }
    }
    response
}
