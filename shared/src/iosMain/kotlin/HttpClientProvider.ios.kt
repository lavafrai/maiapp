package ru.lavafrai.maiapp

import io.ktor.client.engine.*
import io.ktor.client.engine.darwin.*

actual fun platformHttpClientProvider(): HttpClientEngineFactory<*> = Darwin