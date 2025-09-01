@file:OptIn(ExperimentalUuidApi::class)

package ru.lavafrai.maiapp.models.events

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.plus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.lavafrai.maiapp.models.schedule.LessonType
import ru.lavafrai.maiapp.models.time.DateRange
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


@Serializable
@SerialName("simple")
data class SimpleEvent(
    val name: String,
    val date: LocalDate,
    val endDate: LocalDate?,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val room: List<String>,
    val teachers: List<String>,
    val eventType: LessonType,
    val period: SimpleEventPeriod,
    @SerialName("uuid") val _uuid: Uuid,
) : Event() {
    override val uid: Uuid
        get() = _uuid

    override fun renderForDateRange(dateRange: DateRange?): List<RenderedEvent> {
        when (period) {
            SimpleEventPeriod.Single -> {
                if (dateRange?.contains(date) == false) return emptyList()
                return listOf(
                    RenderedEvent(
                        date = date,
                        startTime = startTime,
                        endTime = endTime,
                        name = name,
                        teachers = teachers,
                        classrooms = room,
                        type = eventType,
                        uuid = _uuid,
                    )
                )
            }

            SimpleEventPeriod.Weekly, SimpleEventPeriod.Biweekly -> {
                endDate!!
                val _dateRange = dateRange ?: DateRange(date, endDate)
                val result = mutableListOf<RenderedEvent>()
                var currentDate = maxOf(date, _dateRange.startDate)

                while (currentDate <= minOf(endDate, _dateRange.endDate)) {
                    if ((currentDate.toEpochDays() - date.toEpochDays()) % period.datePeriod.days == 0) {
                        result.add(
                            RenderedEvent(
                                date = currentDate,
                                startTime = startTime,
                                endTime = endTime,
                                name = name,
                                teachers = teachers,
                                classrooms = room,
                                type = eventType,
                                uuid = _uuid,
                            )
                        )
                    }
                    currentDate = currentDate.plus(DatePeriod(days = 1))
                }

                return result
            }

            SimpleEventPeriod.Monthly -> {
                endDate!!
                val _dateRange = dateRange ?: DateRange(date, endDate)
                val result = mutableListOf<RenderedEvent>()
                var currentDate = maxOf(date, _dateRange.startDate)

                while (currentDate <= minOf(endDate, _dateRange.endDate)) {
                    if (currentDate.dayOfMonth == date.dayOfMonth) {
                        result.add(
                            RenderedEvent(
                                date = currentDate,
                                startTime = startTime,
                                endTime = endTime,
                                name = name,
                                teachers = teachers,
                                classrooms = room,
                                type = eventType,
                                uuid = _uuid,
                            )
                        )
                    }
                    currentDate = currentDate.plus(DatePeriod(days = 1))
                }

                return result
            }
        }
    }
}
