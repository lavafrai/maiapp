mod admin;
mod response;
mod routes;

use axum::{Router, http::HeaderValue};
use tower_http::{cors::CorsLayer, services::ServeDir, trace::TraceLayer};

use crate::{config::AppConfig, state::AppState};

pub fn router(config: AppConfig, state: AppState) -> anyhow::Result<Router> {
    let cors = CorsLayer::new()
        .allow_origin(
            config
                .cors_origins
                .iter()
                .map(|origin| origin.parse::<HeaderValue>())
                .collect::<Result<Vec<_>, _>>()?,
        )
        .allow_headers([axum::http::header::CONTENT_TYPE, axum::http::header::IF_NONE_MATCH])
        .expose_headers([axum::http::header::ETAG, axum::http::HeaderName::from_static("x-maiapp-publication-state")])
        .allow_methods([
            axum::http::Method::GET,
            axum::http::Method::HEAD,
            axum::http::Method::OPTIONS,
        ]);

    Ok(Router::new()
        .merge(routes::router())
        .merge(crate::publication::http::router())
        .merge(admin::router(config.clone()))
        .nest_service("/assets", ServeDir::new(config.assets_dir))
        .layer(cors)
        .layer(TraceLayer::new_for_http())
        .with_state(state))
}
