package ru.lavafrai.maiapp.platform

import com.russhwolf.settings.Settings
import io.ktor.client.engine.js.*
import kotlinx.browser.document

class WebPlatform: Platform {
    override fun name() = "Web"
    override fun ktorEngine() = Js
    override fun dispatchers(): Dispatchers = Dispatchers(
        IO = kotlinx.coroutines.Dispatchers.Default,
        Main = kotlinx.coroutines.Dispatchers.Default,
        Default = kotlinx.coroutines.Dispatchers.Default
    )
    override fun storage() = Settings()

    // The sign in reads Set-Cookie and redirects and sets Cookie itself, which browsers forbid,
    // and my.mai.ru doesn't allow CORS; proxying would pass the password through our server
    override fun supportsMyMaiAccount() = false

    // Images are drawn on a canvas, so a browser gives their pixels only with CORS headers
    override fun canLoadCrossOriginImages() = false
    override fun openUrl(url: String) {
        document.defaultView?.open(url, "_blank")
    }
}