@file:OptIn(ExperimentalSerializationApi::class, ExperimentalSettingsApi::class)

package ru.lavafrai.maiapp.data.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.russhwolf.settings.ExperimentalSettingsApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import ru.lavafrai.maiapp.models.schedule.BaseScheduleId
import ru.lavafrai.maiapp.platform.getPlatform
import ru.lavafrai.maiapp.theme.colorSchemas.DefaultColorSchema
import ru.lavafrai.maiapp.theme.themes.SystemTheme


@Serializable
data class ApplicationSettingsData(
    val selectedSchedule: BaseScheduleId? = null,
    val savedSchedules: List<BaseScheduleId> = emptyList(),
    val theme: String = SystemTheme().id,
    val colorSchema: String = DefaultColorSchema().id,
    val hideMilitaryTraining: Boolean = false,

    // official account
    val selectedStudentId: Int? = null,
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
            val settingsSerialized =
                storage.getStringOrNull("settings") ?: return ApplicationSettingsData()
            Json.decodeFromString<ApplicationSettingsData>(settingsSerialized)
        } catch (e: SerializationException) {
            return ApplicationSettingsData()
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

    suspend fun setSelectedGroup(group: BaseScheduleId) {
        mutex.withLock {
            val current = getCurrent()
            update(current.copy(selectedSchedule = group))
        }
    }

    suspend fun addSavedGroup(group: BaseScheduleId) {
        mutex.withLock {
            val current = getCurrent()
            val newSavedGroups = current.savedSchedules
                .toMutableList()
                .apply { add(group) }
                .distinctBy { it.scheduleId }
            update(current.copy(savedSchedules = newSavedGroups))
        }
    }

    suspend fun removeSavedGroup(group: BaseScheduleId) {
        mutex.withLock {
            val current = getCurrent()
            val newSavedGroups = current.savedSchedules
                .toMutableList()
                .apply { remove(group) }
                .distinctBy { it.id }
            update(current.copy(savedSchedules = newSavedGroups))
        }
    }

    suspend fun setSelectedSchedule(schedule: BaseScheduleId) {
        val tmp = getCurrent()
        if (tmp.selectedSchedule != null) addSavedGroup(tmp.selectedSchedule)

        mutex.withLock {
            val current = getCurrent()
            update(current.copy(selectedSchedule = schedule))
        }
    }

    fun setHideMilitaryTraining(hide: Boolean) {
        val current = getCurrent()
        update(current.copy(hideMilitaryTraining = hide))
    }

    fun setSelectedStudentId(student: Int) {
        val current = getCurrent()
        update(current.copy(selectedStudentId = student))
    }
}

fun getSettings(): ApplicationSettingsData = ApplicationSettings.getCurrent()


@Composable
fun rememberSettings() = ApplicationSettings.state.collectAsState()
