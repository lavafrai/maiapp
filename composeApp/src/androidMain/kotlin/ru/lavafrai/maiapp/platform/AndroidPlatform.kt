package ru.lavafrai.maiapp.platform

import ru.lavafrai.maiapp.AndroidApplication
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.materialkolor.dynamicColorScheme
import com.russhwolf.settings.Settings
import io.ktor.client.engine.cio.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.lavafrai.maiapp.ru.lavafrai.maiapp.platform.AndroidChromeView
import ru.lavafrai.maiapp.ru.lavafrai.maiapp.widget.ScheduleWidgetReceiver
import ru.lavafrai.maiapp.theme.ApplicationColorSchema
import ru.lavafrai.maiapp.theme.colorSchemas.MonetColorSchema


class AndroidPlatform: Platform {
    val context = AndroidApplication.instance()

    override fun name() = "Android"
    override fun ktorEngine() = CIO
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