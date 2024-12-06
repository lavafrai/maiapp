package ru.lavafrai.maiapp.platform

import io.ktor.client.engine.*
import com.russhwolf.settings.Settings

interface Platform {
    fun name(): String
    fun ktorEngine(): HttpClientEngineFactory<*>
    fun dispatchers(): Dispatchers
    fun storage(): Settings
    fun openUrl(url: String)
}