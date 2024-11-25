package ru.lavafrai.maiapp.platform

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isOutOfBounds
import androidx.compose.ui.input.pointer.pointerInput
import com.russhwolf.settings.Settings
import com.russhwolf.settings.MapSettings
import io.ktor.client.engine.*
import io.ktor.client.engine.js.*
import kotlinx.browser.document
import ru.lavafrai.maiapp.data.Storage

actual fun getPlatformName(): String {
    return "Web"
}

actual fun getPlatformKtorEngine(): HttpClientEngineFactory<*> {
    return Js
}

actual fun Modifier.pointerCursor(): Modifier = composed {
    val hovered = remember { mutableStateOf(false) }

    if (hovered.value) {
        document.body?.style?.cursor = "pointer"
    } else {
        document.body?.style?.cursor = "default"
    }

    this.pointerInput(Unit) {
        awaitPointerEventScope {
            while (true) {
                val pass = PointerEventPass.Main
                val event = awaitPointerEvent(pass)
                val isOutsideRelease = event.type == PointerEventType.Release &&
                        event.changes[0].isOutOfBounds(size, Size.Zero)
                hovered.value = event.type != PointerEventType.Exit && !isOutsideRelease
            }
        }
    }
}

actual fun getPlatformDispatchers(): Dispatchers = Dispatchers(
    IO = kotlinx.coroutines.Dispatchers.Default,
    Main = kotlinx.coroutines.Dispatchers.Default,
    Default = kotlinx.coroutines.Dispatchers.Default
)

actual fun getPlatformSettingsStorage(): Settings = Settings()