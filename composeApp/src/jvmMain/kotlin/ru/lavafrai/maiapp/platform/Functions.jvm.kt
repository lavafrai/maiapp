package ru.lavafrai.maiapp.platform

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.input.PlatformImeOptions
import coil3.ImageLoader


actual fun Modifier.pointerCursor(): Modifier = pointerHoverIcon(PointerIcon.Hand, true)

actual fun getPlatform(): Platform = JvmPlatform()

actual fun credentialImeOptions(field: CredentialField): PlatformImeOptions? = null

actual fun ImageLoader.Builder.trimMemoryCacheInBackground(): ImageLoader.Builder = this

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
actual fun windowSizeClass(): WindowSizeClass = calculateWindowSizeClass()
