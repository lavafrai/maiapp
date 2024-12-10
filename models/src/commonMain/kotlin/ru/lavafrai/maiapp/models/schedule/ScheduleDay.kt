package ru.lavafrai.maiapp.models.schedule

import kotlinx.datetime.LocalDate
import kotlinx.datetime.serializers.LocalDateComponentSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.lavafrai.maiapp.models.time.DayOfWeek

@Serializable
data class ScheduleDay(
    @SerialName("date") @Serializable(LocalDateComponentSerializer::class) val date: LocalDate,
    @SerialName("day") val dayOfWeek: DayOfWeek,
    @SerialName("lessons") val lessons: List<Lesson>
)
