package ru.lavafrai.maiapp.platform

import androidx.compose.ui.Modifier
import com.russhwolf.settings.Settings
import io.ktor.client.engine.*
import io.ktor.client.engine.darwin.*
import kotlinx.coroutines.IO
import ru.lavafrai.maiapp.data.Storage

actual fun Modifier.pointerCursor(): Modifier = this

actual fun getPlatform(): Platform = IOSPlatform()
