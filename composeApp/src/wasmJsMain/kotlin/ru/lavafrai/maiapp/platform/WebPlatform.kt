package ru.lavafrai.maiapp.platform

import com.russhwolf.settings.Settings
import io.ktor.client.engine.js.*
import kotlinx.browser.document
import org.w3c.dom.Document

class WebPlatform: Platform {
    override fun name() = "Web"
    override fun ktorEngine() = Js
    override fun dispatchers(): Dispatchers = Dispatchers(
        IO = kotlinx.coroutines.Dispatchers.Default,
        Main = kotlinx.coroutines.Dispatchers.Default,
        Default = kotlinx.coroutines.Dispatchers.Default
    )
    override fun storage() = Settings()
    override fun openUrl(url: String) {
        document.defaultView?.open(url, "_blank")
    }
}