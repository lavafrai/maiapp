package ru.lavafrai.maiapp

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.android.*
import io.ktor.client.engine.cio.*

actual fun platformHttpClientProvider(): HttpClientEngineFactory<*> = CIO

actual fun createMyMaiHttpClient(): HttpClient = HttpClient(Android) {
    engine {
        sslManager = { connection ->
            if (isMyMaiHost(connection.url.host)) {
                connection.sslSocketFactory = myMaiSocketFactory
            }
        }
    }
    configureMyMaiClient()
}
