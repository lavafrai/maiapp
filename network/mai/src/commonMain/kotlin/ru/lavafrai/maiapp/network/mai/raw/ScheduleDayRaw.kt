package ru.lavafrai.maiapp.network.mai.raw

import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import ru.lavafrai.maiapp.models.schedule.ScheduleDay
import ru.lavafrai.maiapp.models.time.DayOfWeek
import ru.lavafrai.maiapp.models.time.RussianDateFormat
import kotlinx.serialization.json.decodeFromJsonElement

@Serializable
data class ScheduleDayRaw (
    @SerialName("day") val dayOfWeek: DayOfWeek,
    @SerialName("pairs") val lessons: Map<String, JsonObject>
) {
    fun toScheduleDay(json: Json, dateText: String, teacherLessons: Boolean = false): ScheduleDay {
        val date = LocalDate.parse(dateText, RussianDateFormat)
        return if (teacherLessons) ScheduleDay(
            // if (date != null) { { Date.parseMaiFormat(date)} errorCase {Date.parse(date)} } else null,
            date = date,
            dayOfWeek = dayOfWeek,
            lessons = lessons.map {
                json.decodeFromJsonElement<TeacherLessonRaw>(it.value).toLesson(
                    date,
                )
            }
        )
        else ScheduleDay(
            date = date,
            dayOfWeek = dayOfWeek,
            lessons = lessons.map {
                json.decodeFromJsonElement<LessonRaw>(it.value.values.first()).toLesson(
                    it.value.keys.first(),
                    date,
                )
            }
        )
    }
}