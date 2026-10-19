package ru.lavafrai.maiapp.platform

/**
 * AppMetrica, implemented by the full flavor of androidApp. It isn't here because the foss flavor for F-Droid
 * must not contain it (it brings proprietary Google Play services), and KMP library modules can't have flavors
 */
interface AndroidAnalytics {
    /** Calls [onResult] once, with null if the ID is unavailable */
    fun requestDeviceId(onResult: (String?) -> Unit)

    fun reportError(message: String, error: Throwable)

    fun trackScreen(screen: String)
}
