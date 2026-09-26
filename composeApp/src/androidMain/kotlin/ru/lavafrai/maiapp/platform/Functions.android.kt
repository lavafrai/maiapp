package ru.lavafrai.maiapp.platform

import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PlatformImeOptions
import coil3.ImageLoader
import coil3.memoryCacheMaxSizePercentWhileInBackground


actual fun Modifier.pointerCursor(): Modifier = this

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun credentialImeOptions(field: CredentialField): PlatformImeOptions? = null

actual fun ImageLoader.Builder.trimMemoryCacheInBackground(): ImageLoader.Builder =
    memoryCacheMaxSizePercentWhileInBackground(0.5)
