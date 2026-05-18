use axum::{
    http::{HeaderMap, HeaderValue, StatusCode},
    response::{IntoResponse, Response},
};

pub type AppResult<T> = Result<T, AppError>;

#[derive(Debug, thiserror::Error)]
pub enum AppError {
    #[error("{0}")]
    NotFound(String),
    #[error("{0}")]
    Upstream(String),
}

impl AppError {
    pub fn not_found(message: impl Into<String>) -> Self {
        Self::NotFound(message.into())
    }

    fn status(&self) -> StatusCode {
        match self {
            Self::NotFound(_) => StatusCode::NOT_FOUND,
            Self::Upstream(_) => StatusCode::BAD_GATEWAY,
        }
    }
}

impl From<anyhow::Error> for AppError {
    fn from(error: anyhow::Error) -> Self {
        Self::Upstream(error.to_string())
    }
}

impl IntoResponse for AppError {
    fn into_response(self) -> Response {
        let message = self.to_string();
        let mut headers = HeaderMap::new();
        if let Ok(value) = HeaderValue::from_str(&message) {
            headers.insert("x-error-message", value);
        }
        (self.status(), headers, message).into_response()
    }
}
