package ru.lavafrai.maiapp.platform

import androidx.compose.ui.Modifier
import io.ktor.client.engine.*


expect fun getPlatformName(): String
expect fun getPlatformKtorEngine(): HttpClientEngineFactory<*>
expect fun Modifier.pointerCursor(): Modifier
