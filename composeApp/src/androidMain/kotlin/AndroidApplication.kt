package ru.lavafrai.maiapp

import co.touchlab.kermit.Logger
import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.AppMetricaConfig
import ru.lavafrai.maiapp.BuildConfig.APPMETRICA_APIKEY
import ru.lavafrai.maiapp.data.settings.ApplicationSettings
import ru.lavafrai.maiapp.localizers.LocalAppLocale

class AndroidApplication : android.app.Application() {
    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()
        // The widget may run in a process without UI, which applies the language otherwise
        LocalAppLocale.apply(ApplicationSettings.getCurrent().language)

        try {
            val config = AppMetricaConfig.newConfigBuilder(APPMETRICA_APIKEY).build()
            AppMetrica.activate(this, config)
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