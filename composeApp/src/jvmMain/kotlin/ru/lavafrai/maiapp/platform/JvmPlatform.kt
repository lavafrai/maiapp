package ru.lavafrai.maiapp.platform

import com.russhwolf.settings.Settings
import io.ktor.client.engine.cio.*
import java.awt.Desktop

class JvmPlatform: Platform {
    override fun name() = "JVM"
    override fun ktorEngine() = CIO
    override fun dispatchers(): Dispatchers = Dispatchers(
        IO = kotlinx.coroutines.Dispatchers.IO,
        Main = kotlinx.coroutines.Dispatchers.Default,
        Default = kotlinx.coroutines.Dispatchers.Default,
    )
    override fun storage(): Settings = DesktopSettings()
    override fun openUrl(url: String) {
        if (!Desktop.isDesktopSupported()) {
            error("Desktop is not supported")
        }
        Desktop.getDesktop().browse(java.net.URI(url))
    }
}