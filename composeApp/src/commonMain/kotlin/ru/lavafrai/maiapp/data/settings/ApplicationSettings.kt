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
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.lavafrai.maiapp.platform.getPlatform
import ru.lavafrai.maiapp.theme.colorSchemas.DefaultColorSchema
import ru.lavafrai.maiapp.theme.themes.SystemTheme


@Serializable
data class ApplicationSettingsData(
    val selectedSchedule: String? = null,
    val theme: String = SystemTheme().id,
    val colorSchema: String = DefaultColorSchema().id,
) {
    fun hasSelectedGroup() = selectedSchedule != null
}

@Serializable
object ApplicationSettings {
    private val flow = MutableStateFlow(getCurrent())
    private val storage
        get() = getPlatform().storage()
    private val mutex = Mutex()
    val state = flow as StateFlow<ApplicationSettingsData>

    fun getCurrent(): ApplicationSettingsData {
        val settings = try {
            val settingsSerialized = storage.getStringOrNull("settings") ?: return ApplicationSettingsData()
            Json.decodeFromString<ApplicationSettingsData>(settingsSerialized)
        } catch (e: Exception) {
            e.printStackTrace()
            ApplicationSettingsData()
        }

        return settings
    }

    private fun update(new: ApplicationSettingsData) {
        //storage.encodeValue("settings", new)
        val scheduleEncoded = Json.encodeToString(new)
        storage.putString("settings", scheduleEncoded)
        flow.value = (new)
    }

    fun clear() {
        storage.clear()
        update(ApplicationSettingsData())
    }

    fun setTheme(themeId: String) {
        val current = getCurrent()
        update(current.copy(theme = themeId))
    }

    fun setColorScheme(colorSchemaId: String) {
        val current = getCurrent()
        update(current.copy(colorSchema = colorSchemaId))
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
