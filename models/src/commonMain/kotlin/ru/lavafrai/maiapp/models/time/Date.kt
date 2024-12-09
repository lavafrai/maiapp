package ru.lavafrai.maiapp.models.time

import kotlinx.datetime.*
import kotlin.time.Duration.Companion.days

fun LocalDate.Companion.now(): LocalDate {
    val clock: Clock = Clock.System
    return clock.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
}

fun LocalDate.week(): DateRange {
    val startOfWeek = this.minus(this.dayOfWeek.ordinal.toLong(), DateTimeUnit.DAY)
    val endOfWeek = startOfWeek.plus(6, DateTimeUnit.DAY)
    return DateRange(startOfWeek, endOfWeek)
}