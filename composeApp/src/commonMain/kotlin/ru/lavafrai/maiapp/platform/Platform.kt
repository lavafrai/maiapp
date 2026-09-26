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
    suspend fun appMetricaDeviceId(): String? = null

    /** Whether the my.mai.ru account can work here; if not, its page is hidden and nothing of it is started */
    fun supportsMyMaiAccount(): Boolean = true

    /** Whether images of sites without CORS headers can be loaded directly; see loadableImageUrl */
    fun canLoadCrossOriginImages(): Boolean = true

    /** The widget shows texts in the app language, so it's redrawn when that changes */
    fun onLanguageChanged() {}

    fun supportsWidget(): Boolean = false
    fun requestWidgetCreation(): Unit = error("Widget isn't supported on this platform")

    fun supportsShare(): Boolean = false
    fun shareText(text: String): Unit = error("Share isn't supported on this platform")

    fun doesPlatformSupportsMonet(): Boolean = false
    fun getMonet(): ApplicationColorSchema = error("Monet theme isn't supported on this platform")

    /**
     * Sends a handled error to analytics.
     * @param details stack trace without personal data; use it instead of the [error] message, which may contain it
     */
    fun reportError(context: String, error: Throwable, details: String) {}

    /** Whether this exception of the platform HTTP engine means there's no connection; see Throwable.isNoConnectionError */
    fun isNoConnectionError(error: Throwable): Boolean = false

    fun openGitHub() = openUrl(BuildConfig.GITHUB_URL)
    fun openThanks() = openUrl(BuildConfig.THANKS_URL)
}
