package ru.lavafrai.maiapp.platform

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isOutOfBounds
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.PlatformImeOptions
import coil3.ImageLoader
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

actual fun credentialImeOptions(field: CredentialField): PlatformImeOptions? = null

actual fun ImageLoader.Builder.trimMemoryCacheInBackground(): ImageLoader.Builder = this

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
actual fun windowSizeClass(): WindowSizeClass = calculateWindowSizeClass()
