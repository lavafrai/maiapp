package ru.lavafrai.maiapp.platform

import com.russhwolf.settings.Settings
import io.ktor.client.engine.darwin.*
import kotlinx.coroutines.IO

class IOSPlatform: Platform {
    override fun name() = "iOS"
    override fun ktorEngine() = Darwin
    override fun dispatchers() = Dispatchers(
        IO = kotlinx.coroutines.Dispatchers.IO,
        Main = kotlinx.coroutines.Dispatchers.Main,
        Default = kotlinx.coroutines.Dispatchers.Default
    )
    override fun storage() = Settings()
    override fun openUrl(url: String) {
        error("Not implemented")
        /* TODO: Implement ios web browser */
    }
}