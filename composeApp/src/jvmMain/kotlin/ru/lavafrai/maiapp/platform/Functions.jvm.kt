package ru.lavafrai.maiapp.platform

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.input.PlatformImeOptions
import coil3.ImageLoader


actual fun Modifier.pointerCursor(): Modifier = pointerHoverIcon(PointerIcon.Hand, true)

actual fun getPlatform(): Platform = JvmPlatform()

actual fun credentialImeOptions(field: CredentialField): PlatformImeOptions? = null

actual fun ImageLoader.Builder.trimMemoryCacheInBackground(): ImageLoader.Builder = this
