use std::{path::PathBuf, time::Duration};

#[derive(Clone, Debug)]
pub struct AppConfig {
    pub port: u16,
    pub assets_dir: PathBuf,
    pub telemetry_path: PathBuf,
    pub cache_dir: PathBuf,
    pub admin_username: String,
    pub admin_password: String,
    pub cors_origins: Vec<String>,
    pub connect_timeout: Duration,
    pub request_timeout: Duration,
    pub pool_idle_timeout: Duration,
    pub upstream_probe_interval: Duration,
    pub telemetry_flush_interval: Duration,
    pub telemetry_retention_days: i64,
    pub cache_fresh_ttl: Duration,
    pub cache_valid_ttl: Duration,
    pub cache_worker_timeout: Duration,
    pub cache_max_workers: usize,
}

impl AppConfig {
    pub fn from_env() -> Self {
        Self {
            port: env_parse("PORT").unwrap_or(80),
            assets_dir: std::env::var("MAIAPP_ASSETS_DIR")
                .map(PathBuf::from)
                .unwrap_or_else(|_| PathBuf::from("server/src/main/resources/data/assets")),
            telemetry_path: std::env::var("MAIAPP_TELEMETRY_PATH")
                .map(PathBuf::from)
                .unwrap_or_else(|_| PathBuf::from("/var/lib/maiapp-server3/telemetry.json")),
            cache_dir: std::env::var("MAIAPP_CACHE_DIR")
                .map(PathBuf::from)
                .unwrap_or_else(|_| PathBuf::from("/var/lib/maiapp-server3/cache")),
            admin_username: std::env::var("MAIAPP_ADMIN_USER")
                .unwrap_or_else(|_| "admin".to_owned()),
            admin_password: std::env::var("MAIAPP_ADMIN_PASSWORD")
                .unwrap_or_else(|_| "admin".to_owned()),
            cors_origins: std::env::var("MAIAPP_CORS_ORIGINS")
                .map(|origins| {
                    origins
                        .split(',')
                        .map(|origin| origin.trim().to_owned())
                        .collect()
                })
                .unwrap_or_else(|_| {
                    vec![
                        "https://maiapp.lavafrai.ru".to_owned(),
                        "https://mai3.lavafrai.ru".to_owned(),
                        "https://lavafrai.github.io".to_owned(),
                        "http://127.0.0.1:80".to_owned(),
                        "http://localhost:8080".to_owned(),
                    ]
                }),
            connect_timeout: Duration::from_secs(
                env_parse("MAIAPP_CONNECT_TIMEOUT_SECONDS").unwrap_or(10),
            ),
            request_timeout: Duration::from_secs(
                env_parse("MAIAPP_REQUEST_TIMEOUT_SECONDS").unwrap_or(30),
            ),
            pool_idle_timeout: Duration::from_secs(
                env_parse("MAIAPP_POOL_IDLE_TIMEOUT_SECONDS").unwrap_or(90),
            ),
            upstream_probe_interval: Duration::from_secs(
                env_parse("MAIAPP_UPSTREAM_PROBE_INTERVAL_SECONDS").unwrap_or(60),
            ),
            telemetry_flush_interval: Duration::from_secs(
                env_parse("MAIAPP_TELEMETRY_FLUSH_INTERVAL_SECONDS").unwrap_or(30),
            ),
            telemetry_retention_days: env_parse("MAIAPP_TELEMETRY_RETENTION_DAYS").unwrap_or(30),
            cache_fresh_ttl: Duration::from_secs(
                env_parse("MAIAPP_CACHE_FRESH_TTL_SECONDS").unwrap_or(15 * 60),
            ),
            cache_valid_ttl: Duration::from_secs(
                env_parse("MAIAPP_CACHE_VALID_TTL_SECONDS").unwrap_or(6 * 60 * 60),
            ),
            cache_worker_timeout: Duration::from_secs(
                env_parse("MAIAPP_CACHE_WORKER_TIMEOUT_SECONDS").unwrap_or(45),
            ),
            cache_max_workers: env_parse("MAIAPP_CACHE_MAX_WORKERS").unwrap_or(8),
        }
    }

    pub fn http_client(&self) -> anyhow::Result<reqwest::Client> {
        Ok(reqwest::Client::builder()
            .connect_timeout(self.connect_timeout)
            .timeout(self.request_timeout)
            .pool_idle_timeout(self.pool_idle_timeout)
            .user_agent("MAIapp server3/0.1")
            .build()?)
    }
}

fn env_parse<T: std::str::FromStr>(name: &str) -> Option<T> {
    std::env::var(name)
        .ok()
        .and_then(|value| value.parse().ok())
}
