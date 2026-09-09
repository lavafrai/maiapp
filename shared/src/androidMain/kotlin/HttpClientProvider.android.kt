package ru.lavafrai.maiapp

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.android.*
import io.ktor.client.engine.cio.*

actual fun platformHttpClientProvider(): HttpClientEngineFactory<*> = CIO

// HttpsURLConnection honors the domain-scoped Android Network Security Config.
actual fun createMyMaiHttpClient(): HttpClient = HttpClient(Android) {
    configureMyMaiClient()
}
