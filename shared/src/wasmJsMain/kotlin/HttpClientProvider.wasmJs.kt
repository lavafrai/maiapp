package ru.lavafrai.maiapp

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*

actual fun platformHttpClientProvider(): HttpClientEngineFactory<*> = Js

actual fun createMyMaiHttpClient(): HttpClient = HttpClient(Js) {
    install(ContentNegotiation) {
        json(JsonProvider.tolerantJson)
    }
}