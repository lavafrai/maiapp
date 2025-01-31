package ru.lavafrai.maiapp.network.mai.raw

import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.lavafrai.maiapp.models.schedule.Classroom
import ru.lavafrai.maiapp.models.schedule.Lesson
import ru.lavafrai.maiapp.models.schedule.LessonType
import ru.lavafrai.maiapp.models.schedule.TeacherId
import ru.lavafrai.maiapp.utils.capitalizeWords

@Serializable
data class LessonRaw(
    @SerialName("time_start") val timeStart: TimeRaw,
    @SerialName("time_end") val timeEnd: TimeRaw,
    val lector: Map<String, String>,
    val type: Map<LessonType, Int>,
    val room: Map<String, String>,
    val lms: String,
    val teams: String,
    val other: String,
) {
    fun toLesson(name: String, day: LocalDate): Lesson {
        return Lesson(
            name = name,
            timeStart = timeStart.toTime(),
            timeEnd = timeEnd.toTime(),
            lectors = lector.map { TeacherId(it.value.capitalizeWords(), it.key) },
            type = type.map { it.key }.first(),
            day = day,
            rooms = room.map { Classroom(it.value, it.key) },
            lms = lms,
            teams = teams,
            other = other,
        )
    }
}