package ru.lavafrai.maiapp

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*

object HttpClientProvider {
    val default = HttpClient(platformHttpClientProvider()) {
        install(ContentNegotiation) {
            json(JsonProvider.tolerantJson)
        }
    }

    val myMai by lazy { createMyMaiHttpClient() }
}

expect fun platformHttpClientProvider(): HttpClientEngineFactory<*>

expect fun createMyMaiHttpClient(): HttpClient
