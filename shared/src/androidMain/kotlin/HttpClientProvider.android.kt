package ru.lavafrai.maiapp

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.android.*

// All Android clients must use hostname-aware platform trust with Network Security Config.
actual fun platformHttpClientProvider(): HttpClientEngineFactory<*> = Android

// HttpsURLConnection honors the domain-scoped Android Network Security Config.
actual fun createMyMaiHttpClient(): HttpClient = HttpClient(Android) {
    configureMyMaiClient()
}
