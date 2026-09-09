package ru.lavafrai.maiapp

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*

actual fun platformHttpClientProvider(): HttpClientEngineFactory<*> = CIO

actual fun createMyMaiHttpClient(): HttpClient = HttpClient(CIO) {
    engine {
        https {
            trustManager = MaiDomainTrustManager.instance
        }
    }

    install(ContentNegotiation) {
        json(JsonProvider.tolerantJson)
    }
}