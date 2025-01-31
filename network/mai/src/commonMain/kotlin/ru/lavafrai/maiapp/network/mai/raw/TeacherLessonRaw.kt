package ru.lavafrai.maiapp.network.mai.raw

import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.lavafrai.maiapp.models.schedule.Classroom
import ru.lavafrai.maiapp.models.schedule.Lesson
import ru.lavafrai.maiapp.models.schedule.LessonType
import ru.lavafrai.maiapp.models.schedule.TeacherId

@Serializable
data class TeacherLessonRaw(
    @SerialName("time_start") val timeStart: TimeRaw,
    @SerialName("time_end") val timeEnd: TimeRaw,
    val name: String,
    // val lector: Map<String, String>,
    val groups: List<String>,
    val types: List<LessonType>,
    val rooms: Map<String, String>,
) {
    fun toLesson(day: LocalDate): Lesson {
        return Lesson(
            name = name,
            timeStart = timeStart.toTime(),
            timeEnd = timeEnd.toTime(),
            lectors = groups.map { TeacherId(it, it) },
            type = types.first(),
            day = day,
            rooms = rooms.map { Classroom(it.value, it.key) },
            lms = "",
            teams = "",
            other = "",
        )
    }
}