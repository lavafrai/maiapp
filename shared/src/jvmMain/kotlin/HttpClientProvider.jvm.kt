package ru.lavafrai.maiapp

import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*

actual fun platformHttpClientProvider(): HttpClientEngineFactory<*> = CIO