package ru.lavafrai.maiapp.platform

import io.ktor.client.engine.*
import io.ktor.client.engine.js.*

actual fun getPlatformName(): String {
    return "Web"
}

actual fun getPlatformKtorEngine(): HttpClientEngineFactory<*> {
    return Js
}