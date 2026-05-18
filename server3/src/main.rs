mod cache;
mod config;
mod errors;
mod http;
mod models;
mod repositories;
mod services;
mod state;
mod telemetry;

use std::{net::SocketAddr, sync::Arc};

use config::AppConfig;
use repositories::{exler::ExlerRepository, mai::MaiRepository};
use services::{data::MaiDataService, exler::ExlerService, schedule::ScheduleService};
use state::AppState;
use telemetry::Telemetry;
use tracing_subscriber::{EnvFilter, layer::SubscriberExt, util::SubscriberInitExt};

#[tokio::main]
async fn main() -> anyhow::Result<()> {
    init_tracing();

    let config = AppConfig::from_env();
    let client = config.http_client()?;
    let telemetry = Arc::new(Telemetry::load_or_new(&config).await);
    let mai_repository = Arc::new(MaiRepository::new(
        client.clone(),
        &config,
        telemetry.clone(),
    ));
    let exler_repository = Arc::new(ExlerRepository::new(client, &config, telemetry.clone()));

    let state = AppState::new(
        Arc::new(ScheduleService::new(mai_repository)),
        Arc::new(ExlerService::new(exler_repository)),
        Arc::new(MaiDataService::new()),
        telemetry.clone(),
    );

    telemetry.clone().spawn_upstream_monitor(config.clone());
    telemetry.clone().spawn_persistence(config.clone());
    let app = http::router(config.clone(), state)?;
    let addr = SocketAddr::from(([0, 0, 0, 0], config.port));
    tracing::info!("server3 listening on {addr}");
    let listener = tokio::net::TcpListener::bind(addr).await?;
    axum::serve(listener, app)
        .with_graceful_shutdown(shutdown_signal())
        .await?;
    Ok(())
}

fn init_tracing() {
    tracing_subscriber::registry()
        .with(
            EnvFilter::try_from_default_env()
                .unwrap_or_else(|_| "server3=info,tower_http=info".into()),
        )
        .with(tracing_subscriber::fmt::layer())
        .init();
}

async fn shutdown_signal() {
    let _ = tokio::signal::ctrl_c().await;
}
