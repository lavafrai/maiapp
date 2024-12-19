package ru.lavafrai.maiapp.data.settings

import ru.lavafrai.maiapp.BuildConfig
import ru.lavafrai.maiapp.platform.getPlatform

object VersionInfo {
    val lastVersion: String?
        get() = getPlatform().storage().getStringOrNull("lastVersion")

    val currentVersion: String
        get() = BuildConfig.VERSION_NAME

    private fun setLastVersion(version: String) = getPlatform().storage().putString("lastVersion", version)

    fun updateLastVersion() = setLastVersion(currentVersion)

    fun hasBeenUpdated(): Boolean = lastVersion != currentVersion
}