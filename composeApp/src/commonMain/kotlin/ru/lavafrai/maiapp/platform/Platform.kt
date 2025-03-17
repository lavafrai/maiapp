package ru.lavafrai.maiapp.platform

import com.russhwolf.settings.Settings
import io.ktor.client.engine.HttpClientEngineFactory
import ru.lavafrai.maiapp.BuildConfig
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

    fun openGitHub() = openUrl(BuildConfig.GITHUB_URL)
    fun openThanks() = openUrl(BuildConfig.THANKS_URL)
}