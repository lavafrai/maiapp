package ru.lavafrai.maiapp.platform

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon


actual fun Modifier.pointerCursor(): Modifier = pointerHoverIcon(PointerIcon.Hand, true)

actual fun getPlatform(): Platform = JvmPlatform()
