package ru.lavafrai.maiapp.platform

import io.ktor.client.engine.*
import io.ktor.client.engine.darwin.*

actual fun getPlatformName(): String {
    return "iOS"
}

actual fun getPlatformKtorEngine(): HttpClientEngineFactory<*> {
    return Darwin
}