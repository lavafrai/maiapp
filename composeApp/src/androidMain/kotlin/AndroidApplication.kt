package ru.lavafrai.maiapp

import co.touchlab.kermit.Logger
import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.AppMetricaConfig
import ru.lavafrai.maiapp.BuildConfig.APPMETRICA_APIKEY

class AndroidApplication : android.app.Application() {
    override fun onCreate() {
        instance = this
        super.onCreate()

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