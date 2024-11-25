package ru.lavafrai.maiapp.platform

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*

actual fun getPlatformName(): String = "JVM"

actual fun getPlatformKtorEngine(): HttpClientEngineFactory<*> = CIO

actual fun Modifier.pointerCursor(): Modifier = pointerHoverIcon(PointerIcon.Hand, true)

actual fun getPlatformDispatchers(): Dispatchers = Dispatchers(
    IO = kotlinx.coroutines.Dispatchers.IO,
    Main = kotlinx.coroutines.Dispatchers.Default,
    Default = kotlinx.coroutines.Dispatchers.Default,
)