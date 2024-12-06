package ru.lavafrai.maiapp.platform

import ru.lavafrai.maiapp.AndroidApplication
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.UriHandler
import com.russhwolf.settings.Settings
import io.ktor.client.engine.cio.*
import ru.lavafrai.maiapp.ru.lavafrai.maiapp.platform.AndroidChromeView


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
}