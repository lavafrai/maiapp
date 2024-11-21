package ru.lavafrai.maiapp.platform

import androidx.compose.ui.Modifier
import io.ktor.client.engine.*
import io.ktor.client.engine.darwin.*

actual fun getPlatformName(): String {
    return "iOS"
}

actual fun getPlatformKtorEngine(): HttpClientEngineFactory<*> {
    return Darwin
}

actual fun Modifier.pointerCursor(): Modifier = this