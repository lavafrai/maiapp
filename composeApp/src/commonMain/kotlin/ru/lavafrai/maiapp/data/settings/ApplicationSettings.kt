@file:OptIn(ExperimentalSerializationApi::class, ExperimentalSettingsApi::class)

package ru.lavafrai.maiapp.data.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.serialization.decodeValueOrNull
import com.russhwolf.settings.serialization.encodeValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import ru.lavafrai.maiapp.platform.getPlatformSettingsStorage
import ru.lavafrai.maiapp.theme.themes.SystemTheme


@Serializable
data class ApplicationSettingsData(
    val selectedSchedule: String? = null,
    val theme: String = SystemTheme().id,
) {
    fun hasSelectedGroup() = selectedSchedule != null
}

@Serializable
object ApplicationSettings {
    private val flow = MutableStateFlow(getCurrent())
    private val storage
        get() = getPlatformSettingsStorage()
    private val mutex = Mutex()
    val state = flow as StateFlow<ApplicationSettingsData>

    fun getCurrent(): ApplicationSettingsData {
        val settings = try {
            storage.decodeValueOrNull<ApplicationSettingsData>("settings") ?: ApplicationSettingsData()
        } catch (e: Exception) {
            e.printStackTrace()
            ApplicationSettingsData()
        }

        return settings
    }

    suspend fun update(new: ApplicationSettingsData) {
        storage.encodeValue("settings", new)
        flow.emit(new)
    }

    suspend fun clear() {
        storage.clear()
        flow.emit(ApplicationSettingsData())
    }

    suspend fun setTheme(themeId: String) {
        mutex.withLock {
            val current = getCurrent()
            update(current.copy(theme = themeId))
        }
    }

    suspend fun setSelectedGroup(group: String) {
        mutex.withLock {
            val current = getCurrent()
            update(current.copy(selectedSchedule = group))
        }
    }
}

fun getSettings(): ApplicationSettingsData = ApplicationSettings.getCurrent()


@Composable
fun rememberSettings() = ApplicationSettings.state.collectAsState()
