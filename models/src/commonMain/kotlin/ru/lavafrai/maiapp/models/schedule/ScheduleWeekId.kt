package ru.lavafrai.maiapp.models.schedule

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.lavafrai.maiapp.models.time.DateRange


@Serializable
data class ScheduleWeekId(
    @SerialName("number") val number: Int,
    @SerialName("range") val range: DateRange,
)
/*
fun parseScheduleWeek(text: String): ScheduleWeekId {
    val number = text.split(" ").first().toInt()

    return ScheduleWeekId(
        number,
        DateRange.parse(text.removePrefix("$number "))
    )
}
*/