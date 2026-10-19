package ru.lavafrai.maiapp

import co.touchlab.kermit.Logger
import ru.lavafrai.maiapp.BuildConfig.APPMETRICA_APIKEY
import ru.lavafrai.maiapp.data.settings.ApplicationSettings
import ru.lavafrai.maiapp.localizers.LocalAppLocale
import ru.lavafrai.maiapp.platform.AndroidAnalytics

open class AndroidApplication : android.app.Application() {
    /** Null in the foss flavor and if the analytics failed to start */
    var analytics: AndroidAnalytics? = null
        private set

    init {
        instance = this
    }

    /** Overridden by the full flavor of androidApp, see [AndroidAnalytics] */
    protected open fun createAnalytics(apiKey: String): AndroidAnalytics? = null

    override fun onCreate() {
        super.onCreate()
        // The widget may run in a process without UI, which applies the language otherwise
        LocalAppLocale.apply(ApplicationSettings.getCurrent().language)

        try {
            analytics = createAnalytics(APPMETRICA_APIKEY)
        } catch (e: Exception) {
            Logger.e("Error initializing yandex appmetrica $e")
            e.printStackTrace()
        }
    }

    companion object {
        private lateinit var instance: AndroidApplication
        fun instance() = instance
    }
}
