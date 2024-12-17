package ru.lavafrai.maiapp.data.settings

import ru.lavafrai.maiapp.platform.getPlatform

object VersionInfo {
    val lastVersion: String?
        get() = getPlatform().storage().getStringOrNull("lastVersion")

    fun setLastVersion(version: String) = getPlatform().storage().putString("lastVersion", version)
}