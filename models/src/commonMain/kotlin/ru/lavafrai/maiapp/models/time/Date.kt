package ru.lavafrai.maiapp.models.time

import kotlinx.datetime.*
import kotlin.time.Clock

fun LocalDate.Companion.now(): LocalDate {
    val clock: Clock = Clock.System
    return clock.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
}

fun LocalDateTime.Companion.now(): LocalDateTime {
    val clock: Clock = Clock.System
    return clock.now().toLocalDateTime(TimeZone.currentSystemDefault())
}

fun LocalTime.Companion.now(): LocalTime {
    val clock: Clock = Clock.System
    return clock.now().toLocalDateTime(TimeZone.currentSystemDefault()).time
}

fun LocalDate.week(): DateRange = DateRange(
    previousOrSame(kotlinx.datetime.DayOfWeek.MONDAY),
    nextOrSame(kotlinx.datetime.DayOfWeek.SUNDAY),
)