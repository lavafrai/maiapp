package ru.lavafrai.maiapp.data.repositories

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import ru.lavafrai.maiapp.data.settings.ApplicationSettings
import ru.lavafrai.maiapp.models.schedule.LessonLike
import ru.lavafrai.maiapp.models.schedule.ScheduleId
import ru.lavafrai.maiapp.utils.contextual


/**
 * Репозиторий для работы с парами, объединяющий функционал расписания и событий.
 * Используется в виджете и других местах, где глубокая кастомизация действий с парами не требуется.
 */
class AbstractLessonRepository: BaseRepository() {
    val scheduleRepository = ScheduleRepository()
    val eventRepository = EventRepository

    suspend fun loadLessonsFromCacheOrNull(groupId: ScheduleId): SimpleSchedule? {
        val schedule = scheduleRepository.getScheduleFromCacheOrNull(groupId) ?: return null
        val events = eventRepository.listAllEvents(groupId)
        val lessons = (schedule.days.flatMap { it.lessons } as List<LessonLike> + events.flatMap { it.renderForDateRange(null) } as List<LessonLike>)
            .contextual(ApplicationSettings.getCurrent().hideMilitaryTraining) {
                filter { it.name != "Военная подготовка" }
            }


        return SimpleSchedule(
            id = groupId,
            name = schedule.name,
            days = lessons.groupBy { it.date }
        )
    }
}

@Serializable
data class SimpleSchedule(
    val id: ScheduleId,
    val name: String,
    val days: Map<LocalDate, List<LessonLike>>,
)