package ru.lavafrai.maiapp.data

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.Settings
import com.russhwolf.settings.serialization.decodeValueOrNull
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable


@Serializable
data class ApplicationSettings(
    val selectedGroup: String? = null,
) {
    fun hasSelectedGroup() = selectedGroup != null
}


fun getPlatformSettingsStorage(): Settings = Settings()

@OptIn(ExperimentalSerializationApi::class, ExperimentalSettingsApi::class)
fun getSettings(): ApplicationSettings {
    val storage = getPlatformSettingsStorage()
    return storage.decodeValueOrNull<ApplicationSettings>("settings") ?: ApplicationSettings()
}