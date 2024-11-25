package ru.lavafrai.maiapp.platform

import androidx.compose.ui.Modifier
import com.russhwolf.settings.Settings
import io.ktor.client.engine.*
import ru.lavafrai.maiapp.data.Storage


expect fun getPlatformName(): String
expect fun getPlatformKtorEngine(): HttpClientEngineFactory<*>
expect fun Modifier.pointerCursor(): Modifier
expect fun getPlatformDispatchers(): Dispatchers
expect fun getPlatformSettingsStorage(): Settings