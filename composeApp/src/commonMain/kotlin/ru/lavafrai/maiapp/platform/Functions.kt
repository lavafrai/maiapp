package ru.lavafrai.maiapp.platform

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PlatformImeOptions
import coil3.ImageLoader


// expect fun getPlatformName(): String
// expect fun getPlatformKtorEngine(): HttpClientEngineFactory<*>
expect fun Modifier.pointerCursor(): Modifier
// expect fun getPlatformDispatchers(): Dispatchers
// expect fun getPlatformSettingsStorage(): Settings

expect fun getPlatform(): Platform

enum class CredentialField { Username, Password }

expect fun credentialImeOptions(field: CredentialField): PlatformImeOptions?

expect fun ImageLoader.Builder.trimMemoryCacheInBackground(): ImageLoader.Builder

/** calculateWindowSizeClass() of the official library needs an Activity on Android */
@Composable
expect fun windowSizeClass(): WindowSizeClass
