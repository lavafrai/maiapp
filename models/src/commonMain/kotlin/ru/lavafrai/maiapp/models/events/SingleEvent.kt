@file:OptIn(ExperimentalUuidApi::class)

package ru.lavafrai.maiapp.models.events

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.lavafrai.maiapp.models.time.DateRange
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


@Serializable
@SerialName("single")
data class SingleEvent (
    val name: String,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val room: String?,
    val teachers: List<String>,
    val type: EventType,
    @SerialName("uuid") val _uuid: Uuid,
): Event() {
    override val uid: Uuid
        get() = _uuid

    override fun renderForDateRange(dateRange: DateRange): List<RenderedEvent> {
        if (date !in dateRange) return emptyList()
        return listOf(
            RenderedEvent(
                date = date,
                startTime = startTime,
                endTime = endTime,
                name = name,
                teachers = teachers,
                room = room,
                type = type,
                uuid = _uuid,
            )
        )
    }
}
