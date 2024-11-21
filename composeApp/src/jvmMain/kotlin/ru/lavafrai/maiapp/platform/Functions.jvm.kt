package ru.lavafrai.maiapp.platform

import androidx.compose.ui.Modifier
import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*

actual fun getPlatformName(): String = "JVM"

actual fun getPlatformKtorEngine(): HttpClientEngineFactory<*> = CIO

actual fun Modifier.pointerCursor(): Modifier = this