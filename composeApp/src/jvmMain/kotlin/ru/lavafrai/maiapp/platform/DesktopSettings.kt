package ru.lavafrai.maiapp.platform

import com.russhwolf.settings.Settings
import java.io.File

class DesktopSettings : Settings {
    init {
        prepareEnvironment()
    }

    private fun getApplicationStorageDirectory(): String {
        val os = System.getProperty("os.name").lowercase()
        val root = when {
            os.contains("win") -> System.getenv("APPDATA")
            os.contains("mac") -> System.getProperty("user.home") + "/Library/Application Support"
            os.contains("nix") || os.contains("nux") || os.contains("aix") -> System.getProperty("user.home") + "/etc"
            else -> System.getProperty("user.dir")
        }
        return "$root/maiapp"
    }

    private fun prepareEnvironment() {
        val storageDirectory = getApplicationStorageDirectory()
        val storageDirectoryFile = File(storageDirectory)
        if (!storageDirectoryFile.exists()) {
            storageDirectoryFile.mkdirs()
        }
    }

    private fun getStorageFile(key: String): File {
        return File(getApplicationStorageDirectory(), key
            .replace("?",".nullability")
            .replace(":", "@")
        )
    }

    private fun getStorageFileIfExists(key: String): File? {
        val file = getStorageFile(key)
        return if (file.exists()) file else null
    }

    override val keys: Set<String>
        get() = File(getApplicationStorageDirectory()).listFiles()!!.map {
            it.name
                .replace(".nullability", "?")
                .replace("@", ":")
        }.toSet()

    override val size: Int
        get() = File(getApplicationStorageDirectory()).listFiles()!!.size

    override fun clear() = File(getApplicationStorageDirectory()).listFiles()!!.forEach { it.delete() }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return getStringOrNull(key)?.toBoolean() ?: defaultValue
    }

    override fun getBooleanOrNull(key: String): Boolean? {
        return getStringOrNull(key)?.toBoolean()
    }

    override fun getDouble(key: String, defaultValue: Double): Double {
        return getStringOrNull(key)?.toDouble() ?: defaultValue
    }

    override fun getDoubleOrNull(key: String): Double? {
        return getStringOrNull(key)?.toDouble()
    }

    override fun getFloat(key: String, defaultValue: Float): Float {
        return getStringOrNull(key)?.toFloat() ?: defaultValue
    }

    override fun getFloatOrNull(key: String): Float? {
        return getStringOrNull(key)?.toFloat()
    }

    override fun getInt(key: String, defaultValue: Int): Int {
        return getStringOrNull(key)?.toInt() ?: defaultValue
    }

    override fun getIntOrNull(key: String): Int? {
        return getStringOrNull(key)?.toInt()
    }

    override fun getLong(key: String, defaultValue: Long): Long {
        return getStringOrNull(key)?.toLong() ?: defaultValue
    }

    override fun getLongOrNull(key: String): Long? {
        return getStringOrNull(key)?.toLong()
    }

    override fun getString(key: String, defaultValue: String): String {
        return getStorageFileIfExists(key)?.readText() ?: defaultValue
    }

    override fun getStringOrNull(key: String): String? {
        return getStorageFileIfExists(key)?.readText()
    }

    override fun hasKey(key: String): Boolean {
        return getStorageFileIfExists(key) != null
    }

    override fun putBoolean(key: String, value: Boolean) {
        getStorageFile(key).writeText(value.toString())
    }

    override fun putDouble(key: String, value: Double) {
        getStorageFile(key).writeText(value.toString())
    }

    override fun putFloat(key: String, value: Float) {
        getStorageFile(key).writeText(value.toString())
    }

    override fun putInt(key: String, value: Int) {
        getStorageFile(key).writeText(value.toString())
    }

    override fun putLong(key: String, value: Long) {
        getStorageFile(key).writeText(value.toString())
    }

    override fun putString(key: String, value: String) {
        getStorageFile(key).writeText(value)
    }

    override fun remove(key: String) {
        getStorageFile(key).delete()
    }
}