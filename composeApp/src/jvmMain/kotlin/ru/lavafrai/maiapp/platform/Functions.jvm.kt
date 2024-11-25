package ru.lavafrai.maiapp.platform

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import com.russhwolf.settings.Settings
import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*
import ru.lavafrai.maiapp.data.Storage

actual fun getPlatformName(): String = "JVM"

actual fun getPlatformKtorEngine(): HttpClientEngineFactory<*> = CIO

actual fun Modifier.pointerCursor(): Modifier = pointerHoverIcon(PointerIcon.Hand, true)

actual fun getPlatformDispatchers(): Dispatchers = Dispatchers(
    IO = kotlinx.coroutines.Dispatchers.IO,
    Main = kotlinx.coroutines.Dispatchers.Default,
    Default = kotlinx.coroutines.Dispatchers.Default,
)

actual fun getPlatformSettingsStorage(): Settings = Settings()