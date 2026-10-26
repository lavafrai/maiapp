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

use config::{AppConfig, ScheduleProvider};
use repositories::{
    exler::ExlerRepository, local::LocalMaiRepository, mai::MaiRepository,
    schedule::ScheduleRepository,
};
use services::{data::MaiDataService, exler::ExlerService, schedule::ScheduleService};
use state::AppState;
use telemetry::Telemetry;
use tracing_subscriber::{EnvFilter, layer::SubscriberExt, util::SubscriberInitExt};

#[tokio::main]
async fn main() -> anyhow::Result<()> {
    match dotenvy::dotenv() {
        Ok(_) => {}
        Err(error) if error.not_found() => {}
        Err(error) => return Err(error.into()),
    }
    init_tracing();
    rustls::crypto::ring::default_provider()
        .install_default()
        .expect("no other TLS crypto provider is installed yet");

    let config = AppConfig::from_env()?;
    let client = config.http_client()?;
    let telemetry = Arc::new(Telemetry::load_or_new(&config).await);
    let schedule_repository: Arc<dyn ScheduleRepository> = match &config.schedule_provider {
        ScheduleProvider::Mai => Arc::new(MaiRepository::new(
            client.clone(),
            &config,
            telemetry.clone(),
        )),
        ScheduleProvider::Local(root) => {
            Arc::new(LocalMaiRepository::new(root, &config, telemetry.clone()).await?)
        }
    };
    let exler_repository = Arc::new(ExlerRepository::new(client, &config, telemetry.clone()));

    let state = AppState::new(
        Arc::new(ScheduleService::new(schedule_repository)),
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
