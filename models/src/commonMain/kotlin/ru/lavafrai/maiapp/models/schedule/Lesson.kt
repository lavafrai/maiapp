package ru.lavafrai.maiapp.models.schedule

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.serializers.LocalDateComponentSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.lavafrai.maiapp.models.time.Time
import ru.lavafrai.maiapp.models.time.now

@Serializable
data class Lesson(
    @SerialName("name") override val name: String,
    @SerialName("time_start") val timeStart: Time,
    @SerialName("time_end") val timeEnd: Time,
    @SerialName("lectors") val lectors: List<TeacherId>,
    @SerialName("type") override val type: LessonType,
    @SerialName("day") @Serializable(LocalDateComponentSerializer::class) val day: LocalDate,
    @SerialName("rooms") val rooms: List<Classroom>,
    @SerialName("lms") val lms: String,
    @SerialName("teams") val teams: String,
    @SerialName("other") val other: String,
    @SerialName("timeRange") val timeRange: String = "${timeStart.time.substring(0, timeStart.time.indexOf(":", 3))} – ${timeEnd.time.substring(0, timeEnd.time.indexOf(":", 3))}"
): LessonLike {
    fun getPairNumber(): Int {
        return when (timeRange) {
            "9:00 – 10:30" -> 1
            "10:45 – 12:15" -> 2
            "13:00 – 14:30" -> 3
            "14:45 – 16:15" -> 4
            "16:30 – 18:00" -> 5
            "18:15 – 19:45" -> 6
            "20:00 – 21:30" -> 7
            else -> -1
        }
    }

    override fun getUid(): Int {
        return "$name $day ${getPairNumber()}".hashCode()
    }

    fun isFinished(): Boolean {
        if (day > LocalDate.now()) return false
        if (day < LocalDate.now()) return true
        return timeEnd.toLocalTime() < LocalTime.now()
    }

    override val startTime: LocalTime
        get() = timeStart.toLocalTime()

    override val endTime: LocalTime
        get() = timeEnd.toLocalTime()

    override val date: LocalDate
        get() = day

    override val classrooms: List<String>
        get() = rooms.map { it.name }
}