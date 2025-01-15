package ru.lavafrai.maiapp.platform

import ru.lavafrai.maiapp.AndroidApplication
import android.content.Intent
import android.net.Uri
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.russhwolf.settings.Settings
import io.ktor.client.engine.cio.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.lavafrai.maiapp.ru.lavafrai.maiapp.platform.AndroidChromeView
import ru.lavafrai.maiapp.ru.lavafrai.maiapp.widget.ScheduleWidgetReceiver


class AndroidPlatform: Platform {
    override fun name() = "Android"
    override fun ktorEngine() = CIO
    override fun dispatchers() = Dispatchers(
        IO = kotlinx.coroutines.Dispatchers.IO,
        Main = kotlinx.coroutines.Dispatchers.Main,
        Default = kotlinx.coroutines.Dispatchers.Default
    )
    override fun storage() = Settings()
    override fun openUrl(url: String) {
        val context = AndroidApplication.instance()
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        //context.startActivity(browserIntent)
        AndroidChromeView.openTab(context, url)
    }

    override fun supportsWidget(): Boolean = true
    override fun requestWidgetCreation() {
        val context = AndroidApplication.instance()
        GlobalScope.launch {
            GlanceAppWidgetManager(context).requestPinGlanceAppWidget(
                ScheduleWidgetReceiver::class.java, successCallback = null
            )
        }
    }

    override fun supportsShare() = true
    override fun shareText(text: String) {
        val context = AndroidApplication.instance()
        val intent = Intent(Intent.ACTION_SEND)
            .setType("text/plain")
            .putExtra(Intent.EXTRA_SUBJECT, "My application name")
            .putExtra(Intent.EXTRA_TEXT, text)

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val chooser = Intent.createChooser(intent, "Share via")
        chooser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        context.startActivity(chooser)
    }
}