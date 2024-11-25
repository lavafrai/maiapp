package ru.lavafrai.maiapp.platform

import com.russhwolf.settings.Settings
import kotlinx.browser.localStorage
import org.w3c.dom.set

class LocalStorageSettings() : Settings {
    override val keys: Set<String>
        get() = emptySet()

    override val size: Int
        get() = 0

    override fun clear() = keys.forEach { remove(it) }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        TODO("Not yet implemented")
    }

    override fun getBooleanOrNull(key: String): Boolean? {
        TODO("Not yet implemented")
    }

    override fun getDouble(key: String, defaultValue: Double): Double {
        TODO("Not yet implemented")
    }

    override fun getDoubleOrNull(key: String): Double? {
        TODO("Not yet implemented")
    }

    override fun getFloat(key: String, defaultValue: Float): Float {
        TODO("Not yet implemented")
    }

    override fun getFloatOrNull(key: String): Float? {
        TODO("Not yet implemented")
    }

    override fun getInt(key: String, defaultValue: Int): Int {
        TODO("Not yet implemented")
    }

    override fun getIntOrNull(key: String): Int? {
        TODO("Not yet implemented")
    }

    override fun getLong(key: String, defaultValue: Long): Long {
        TODO("Not yet implemented")
    }

    override fun getLongOrNull(key: String): Long? {
        TODO("Not yet implemented")
    }

    override fun getString(key: String, defaultValue: String): String = localStorage.getItem(key) ?: defaultValue

    override fun getStringOrNull(key: String): String? = localStorage.getItem(key)

    override fun hasKey(key: String): Boolean = localStorage.getItem(key) != null

    override fun putBoolean(key: String, value: Boolean) {
        TODO("Not yet implemented")
    }

    override fun putDouble(key: String, value: Double) {
        TODO("Not yet implemented")
    }

    override fun putFloat(key: String, value: Float) {
        TODO("Not yet implemented")
    }

    override fun putInt(key: String, value: Int) {
        TODO("Not yet implemented")
    }

    override fun putLong(key: String, value: Long) {
        TODO("Not yet implemented")
    }

    override fun putString(key: String, value: String) = localStorage.set(key, value)

    override fun remove(key: String) = localStorage.removeItem(key)
}