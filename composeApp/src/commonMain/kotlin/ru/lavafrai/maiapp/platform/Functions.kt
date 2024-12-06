package ru.lavafrai.maiapp.platform

import androidx.compose.ui.Modifier


// expect fun getPlatformName(): String
// expect fun getPlatformKtorEngine(): HttpClientEngineFactory<*>
expect fun Modifier.pointerCursor(): Modifier
// expect fun getPlatformDispatchers(): Dispatchers
// expect fun getPlatformSettingsStorage(): Settings
expect fun getPlatform(): Platform