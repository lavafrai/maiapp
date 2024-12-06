package ru.lavafrai.maiapp.platform

import androidx.compose.ui.Modifier


actual fun Modifier.pointerCursor(): Modifier = this

actual fun getPlatform(): Platform = AndroidPlatform()
