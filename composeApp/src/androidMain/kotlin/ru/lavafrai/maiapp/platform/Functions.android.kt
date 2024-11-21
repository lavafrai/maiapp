package ru.lavafrai.maiapp.platform

import androidx.compose.ui.Modifier
import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*

actual fun getPlatformName(): String {
    return "Android"
}

actual fun getPlatformKtorEngine(): HttpClientEngineFactory<*> {
    return CIO
}

actual fun Modifier.pointerCursor(): Modifier = this