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
import kotlin.time.Clock


/** Преподаватель, чьё расписание или отзывы недавно открывали */
@Serializable
data class RecentTeacher(
    val name: String,
    /** Время открытия: по нему списки правильно сливаются при смене настройки */
    val openedAt: Long,
)


@Serializable
data class ApplicationSettingsData(
    val selectedSchedule: BaseScheduleId? = null,
    val savedSchedules: List<BaseScheduleId> = emptyList(),
    // Недавно открытые преподаватели: отдельно для поиска по расписаниям и по отзывам.
    // При общем списке (shareRecentTeachers) оба списка содержат одно и то же.
    val recentScheduleTeachers: List<RecentTeacher> = emptyList(),
    val recentReviewTeachers: List<RecentTeacher> = emptyList(),
    val shareRecentTeachers: Boolean = true,
    val recentTeachersLimit: Int = 10,
    val theme: String = SystemTheme().id,
    val colorSchema: String = DefaultColorSchema().id,
    val hideMilitaryTraining: Boolean = false,
    val localMode: Boolean = false,
    val developerMode: Boolean = false,
    // BCP 47 tag, null for the system language
    val language: String? = null,

    // official account
    val selectedStudentId: Int? = null,
) {
    fun hasSelectedGroup() = selectedSchedule != null
}

@Serializable
object ApplicationSettings {
    // ignoreUnknownKeys: настройки, записанные более новой версией приложения, не должны сбрасываться
    private val json = Json { ignoreUnknownKeys = true }

    private val flow = MutableStateFlow(getCurrent())
    private val storage
        get() = getPlatform().storage()
    private val mutex = Mutex()
    val state = flow as StateFlow<ApplicationSettingsData>

    fun getCurrent(): ApplicationSettingsData {
        val settings = try {
            val settingsSerialized =
                storage.getStringOrNull("settings") ?: return ApplicationSettingsData()
            json.decodeFromString<ApplicationSettingsData>(settingsSerialized)
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
        val scheduleEncoded = json.encodeToString(new)
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

    /**
     * Запоминает, что открыли расписание ([forReviews] = false) или отзывы преподавателя.
     * При общем списке обновляются оба списка, иначе — только нужный.
     */
    fun markTeacherOpened(name: String, forReviews: Boolean) {
        val current = getCurrent()
        val now = Clock.System.now().epochSeconds

        fun updated(list: List<RecentTeacher>): List<RecentTeacher> {
            return (listOf(RecentTeacher(name, now)) + list)
                .distinctBy { it.name }
                .sortedByDescending { it.openedAt }
                .take(current.recentTeachersLimit)
        }

        val schedule = if (forReviews && !current.shareRecentTeachers) current.recentScheduleTeachers
                       else updated(current.recentScheduleTeachers)
        val reviews = if (!forReviews && !current.shareRecentTeachers) current.recentReviewTeachers
                      else updated(current.recentReviewTeachers)

        update(current.copy(recentScheduleTeachers = schedule, recentReviewTeachers = reviews))
    }

    /** Убирает преподавателя из недавних: при общем списке — из обоих */
    fun removeRecentTeacher(name: String, forReviews: Boolean) {
        val current = getCurrent()

        val schedule = if (forReviews && !current.shareRecentTeachers) current.recentScheduleTeachers
                       else current.recentScheduleTeachers.filterNot { it.name == name }
        val reviews = if (!forReviews && !current.shareRecentTeachers) current.recentReviewTeachers
                      else current.recentReviewTeachers.filterNot { it.name == name }

        update(current.copy(recentScheduleTeachers = schedule, recentReviewTeachers = reviews))
    }

    /**
     * Включает или выключает общий список недавних.
     * При включении два списка сливаются без повторов (у преподавателя остаётся более позднее открытие),
     * при выключении общий список копируется в оба.
     */
    fun setShareRecentTeachers(shared: Boolean) {
        val current = getCurrent()
        val merged = (current.recentScheduleTeachers + current.recentReviewTeachers)
            .groupBy { it.name }
            .map { (_, opened) -> opened.maxBy { it.openedAt } }
            .sortedByDescending { it.openedAt }
            .take(current.recentTeachersLimit)

        update(
            current.copy(
                shareRecentTeachers = shared,
                recentScheduleTeachers = merged,
                recentReviewTeachers = merged,
            )
        )
    }

    /** Меняет, сколько преподавателей запоминать; лишние отбрасываются с конца списка */
    fun setRecentTeachersLimit(limit: Int) {
        val current = getCurrent()
        update(
            current.copy(
                recentTeachersLimit = limit,
                recentScheduleTeachers = current.recentScheduleTeachers.take(limit),
                recentReviewTeachers = current.recentReviewTeachers.take(limit),
            )
        )
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

    fun setLanguage(language: String?) {
        val current = getCurrent()
        update(current.copy(language = language))
    }

    fun setLocalMode(localMode: Boolean) {
        val current = getCurrent()
        update(current.copy(localMode = localMode))
    }

    fun setDeveloperMode(enabled: Boolean) {
        val current = getCurrent()
        update(current.copy(developerMode = enabled))
    }
}

fun getSettings(): ApplicationSettingsData = ApplicationSettings.getCurrent()


@Composable
fun rememberSettings() = ApplicationSettings.state.collectAsState()
