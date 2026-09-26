package ru.lavafrai.maiapp.platform

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import com.russhwolf.settings.Settings
import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.StartupParamsCallback
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.lavafrai.maiapp.AndroidApplication
import ru.lavafrai.maiapp.platformHttpClientProvider
import ru.lavafrai.maiapp.ru.lavafrai.maiapp.platform.AndroidChromeView
import ru.lavafrai.maiapp.ru.lavafrai.maiapp.widget.ScheduleWidget
import ru.lavafrai.maiapp.ru.lavafrai.maiapp.widget.ScheduleWidgetReceiver
import ru.lavafrai.maiapp.theme.ApplicationColorSchema
import ru.lavafrai.maiapp.theme.colorSchemas.MonetColorSchema
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.milliseconds


class AndroidPlatform: Platform {
    val context = AndroidApplication.instance()

    override fun name() = "Android"

    override suspend fun appMetricaDeviceId(): String? = try {
        withTimeoutOrNull(5_000.milliseconds) {
            suspendCancellableCoroutine { continuation ->
                AppMetrica.requestStartupParams(context, object : StartupParamsCallback {
                    override fun onReceive(result: StartupParamsCallback.Result?) {
                        if (continuation.isActive) continuation.resume(result?.deviceId)
                    }

                    override fun onRequestError(
                        reason: StartupParamsCallback.Reason,
                        result: StartupParamsCallback.Result?,
                    ) {
                        if (continuation.isActive) continuation.resume(result?.deviceId)
                    }
                }, listOf(StartupParamsCallback.APPMETRICA_DEVICE_ID, StartupParamsCallback.APPMETRICA_DEVICE_ID_HASH))
            }
        }
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        null
    }

    override fun reportError(context: String, error: Throwable, details: String) {
        // Same stack trace, but the message is taken from the cleaned details
        val reported = Exception(details.lineSequence().first()).apply { stackTrace = error.stackTrace }
        AppMetrica.reportError(context, reported)
    }

    override fun isNoConnectionError(error: Throwable) = error is java.net.UnknownHostException

    @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
    override fun onLanguageChanged() {
        GlobalScope.launch { ScheduleWidget().updateAll(context) }
    }
    override fun ktorEngine() = platformHttpClientProvider()
    override fun dispatchers() = Dispatchers(
        IO = kotlinx.coroutines.Dispatchers.IO,
        Main = kotlinx.coroutines.Dispatchers.Main,
        Default = kotlinx.coroutines.Dispatchers.Default
    )
    override fun storage() = Settings()
    override fun openUrl(url: String) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        //context.startActivity(browserIntent)
        AndroidChromeView.openTab(context, url)
    }

    override fun supportsWidget(): Boolean = true
    override fun requestWidgetCreation() {
        GlobalScope.launch {
            GlanceAppWidgetManager(context).requestPinGlanceAppWidget(
                ScheduleWidgetReceiver::class.java, successCallback = null
            )
        }
    }

    override fun supportsShare() = true
    override fun shareText(text: String) {
        val intent = Intent(Intent.ACTION_SEND)
            .setType("text/plain")
            .putExtra(Intent.EXTRA_SUBJECT, "My application name")
            .putExtra(Intent.EXTRA_TEXT, text)

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val chooser = Intent.createChooser(intent, "Share via")
        chooser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        context.startActivity(chooser)
    }

    override fun doesPlatformSupportsMonet(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    override fun getMonet(): ApplicationColorSchema {
        if (!doesPlatformSupportsMonet()) error("Monet theme isn't supported on this platform")
        val theme = dynamicDarkColorScheme(context)
        return MonetColorSchema @Composable { currentTheme ->
            if (currentTheme.isDark()) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
    }
}
