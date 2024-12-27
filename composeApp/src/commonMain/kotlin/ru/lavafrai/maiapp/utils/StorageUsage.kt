package ru.lavafrai.maiapp.utils

import com.russhwolf.settings.Settings
import okio.utf8Size

fun Settings.getStorageUsage(): Long {
    return keys.fold(0L) { acc, key ->
        val value = this.getStringOrNull(key)?.utf8Size()
        return@fold acc + (value ?: 0L)
    }
}