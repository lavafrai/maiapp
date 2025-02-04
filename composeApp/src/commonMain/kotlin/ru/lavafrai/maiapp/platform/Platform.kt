package ru.lavafrai.maiapp.platform

import androidx.compose.runtime.Composable
import io.ktor.client.engine.*
import com.russhwolf.settings.Settings
import ru.lavafrai.maiapp.theme.ApplicationColorSchema

interface Platform {
    fun name(): String
    fun ktorEngine(): HttpClientEngineFactory<*>
    fun dispatchers(): Dispatchers
    fun storage(): Settings
    fun openUrl(url: String)

    fun supportsWidget(): Boolean = false
    fun requestWidgetCreation(): Unit = error("Widget isn't supported on this platform")

    fun supportsShare(): Boolean = false
    fun shareText(text: String): Unit = error("Share isn't supported on this platform")

    fun doesPlatformSupportsMonet(): Boolean = false
    fun getMonet(): ApplicationColorSchema = error("Monet theme isn't supported on this platform")
}