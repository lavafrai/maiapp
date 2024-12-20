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
import kotlinx.browser.document


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

actual fun getPlatform(): Platform = WebPlatform()
