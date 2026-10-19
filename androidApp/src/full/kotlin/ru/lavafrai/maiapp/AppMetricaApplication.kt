package ru.lavafrai.maiapp

import android.content.Context
import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.AppMetricaConfig
import io.appmetrica.analytics.StartupParamsCallback
import ru.lavafrai.maiapp.platform.AndroidAnalytics

class AppMetricaApplication : AndroidApplication() {
    override fun createAnalytics(apiKey: String): AndroidAnalytics {
        AppMetrica.activate(this, AppMetricaConfig.newConfigBuilder(apiKey).build())
        return AppMetricaAnalytics(this)
    }
}

private class AppMetricaAnalytics(private val context: Context) : AndroidAnalytics {
    override fun requestDeviceId(onResult: (String?) -> Unit) {
        AppMetrica.requestStartupParams(context, object : StartupParamsCallback {
            override fun onReceive(result: StartupParamsCallback.Result?) {
                onResult(result?.deviceId)
            }

            override fun onRequestError(
                reason: StartupParamsCallback.Reason,
                result: StartupParamsCallback.Result?,
            ) {
                onResult(result?.deviceId)
            }
        }, listOf(StartupParamsCallback.APPMETRICA_DEVICE_ID, StartupParamsCallback.APPMETRICA_DEVICE_ID_HASH))
    }

    override fun reportError(message: String, error: Throwable) {
        AppMetrica.reportError(message, error)
    }

    override fun trackScreen(screen: String) {
        AppMetrica.reportEvent("screen_view", mapOf("screen" to screen))
    }
}
