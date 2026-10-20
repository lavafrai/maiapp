package ru.lavafrai.maiapp.platform

import androidx.activity.compose.LocalActivity
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PlatformImeOptions
import coil3.ImageLoader
import coil3.memoryCacheMaxSizePercentWhileInBackground


actual fun Modifier.pointerCursor(): Modifier = this

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun credentialImeOptions(field: CredentialField): PlatformImeOptions? = null

actual fun ImageLoader.Builder.trimMemoryCacheInBackground(): ImageLoader.Builder =
    memoryCacheMaxSizePercentWhileInBackground(0.5)

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
actual fun windowSizeClass(): WindowSizeClass = calculateWindowSizeClass(requireNotNull(LocalActivity.current))
