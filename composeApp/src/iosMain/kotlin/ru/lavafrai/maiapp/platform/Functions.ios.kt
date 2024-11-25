package ru.lavafrai.maiapp.platform

import androidx.compose.ui.Modifier
import io.ktor.client.engine.*
import io.ktor.client.engine.darwin.*
import kotlinx.coroutines.IO

actual fun getPlatformName(): String {
    return "iOS"
}

actual fun getPlatformKtorEngine(): HttpClientEngineFactory<*> {
    return Darwin
}

actual fun Modifier.pointerCursor(): Modifier = this

actual fun getPlatformDispatchers(): Dispatchers = Dispatchers(
    IO = kotlinx.coroutines.Dispatchers.IO,
    Main = kotlinx.coroutines.Dispatchers.Main,
    Default = kotlinx.coroutines.Dispatchers.Default
)