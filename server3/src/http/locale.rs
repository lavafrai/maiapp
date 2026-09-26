use std::convert::Infallible;

use axum::{
    extract::{FromRequestParts, Query},
    http::request::Parts,
};
use serde::Deserialize;

/// Language of the data the app asked for, resolved from the `locale` query parameter
/// that the app adds to every request (like `ru_RU`, `en` or `en_RU`).
///
/// Nothing is translated yet; a handler gets it by taking `locale: RequestLocale`.
#[derive(Clone, Copy, Debug, Default, PartialEq, Eq)]
pub enum RequestLocale {
    /// The language of the source data
    #[default]
    Ru,
    En,
}

impl RequestLocale {
    /// Resolves like the app picks its resources: Russian for Russian, English for any other language.
    /// No parameter (apps before it was added) keeps the source data language
    pub fn resolve(locale: Option<&str>) -> Self {
        let Some(locale) = locale else {
            return Self::default();
        };
        let language = locale.split(['_', '-']).next().unwrap_or_default();
        if language.eq_ignore_ascii_case("ru") {
            Self::Ru
        } else {
            Self::En
        }
    }
}

#[derive(Deserialize)]
struct LocaleQuery {
    locale: Option<String>,
}

impl<S: Send + Sync> FromRequestParts<S> for RequestLocale {
    // A broken locale must not fail the request, it's just resolved to the default
    type Rejection = Infallible;

    async fn from_request_parts(parts: &mut Parts, _state: &S) -> Result<Self, Self::Rejection> {
        let locale = Query::<LocaleQuery>::try_from_uri(&parts.uri)
            .ok()
            .and_then(|Query(query)| query.locale);
        Ok(Self::resolve(locale.as_deref()))
    }
}

#[cfg(test)]
mod tests {
    use axum::{extract::FromRequestParts, http::Request};

    use super::RequestLocale;

    async fn extract(uri: &str) -> RequestLocale {
        let (mut parts, _) = Request::builder().uri(uri).body(()).unwrap().into_parts();
        RequestLocale::from_request_parts(&mut parts, &()).await.unwrap()
    }

    #[test]
    fn resolves_languages() {
        assert_eq!(RequestLocale::resolve(Some("ru_RU")), RequestLocale::Ru);
        assert_eq!(RequestLocale::resolve(Some("ru")), RequestLocale::Ru);
        assert_eq!(RequestLocale::resolve(Some("RU-ru")), RequestLocale::Ru);
        assert_eq!(RequestLocale::resolve(Some("en_US")), RequestLocale::En);
        assert_eq!(RequestLocale::resolve(Some("en_RU")), RequestLocale::En);
        assert_eq!(RequestLocale::resolve(Some("de_DE")), RequestLocale::En);
        assert_eq!(RequestLocale::resolve(Some("")), RequestLocale::En);
    }

    #[test]
    fn keeps_source_language_without_parameter() {
        assert_eq!(RequestLocale::resolve(None), RequestLocale::Ru);
    }

    #[tokio::test]
    async fn extracts_from_query() {
        assert_eq!(extract("/schedule/X?locale=en_US").await, RequestLocale::En);
        assert_eq!(extract("/schedule/X?url=a&locale=ru_RU").await, RequestLocale::Ru);
        assert_eq!(extract("/schedule/X").await, RequestLocale::Ru);
        // Garbage is just some other language
        assert_eq!(extract("/schedule/X?locale=%ZZ").await, RequestLocale::En);
        // A query that can't be parsed doesn't fail the request
        assert_eq!(extract("/schedule/X?locale=en&locale=en").await, RequestLocale::Ru);
    }
}
