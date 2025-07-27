package ru.lavafrai.maiapp.models.time

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
enum class DayOfWeek {
    @SerialName("Пн") MONDAY,
    @SerialName("Вт") TUESDAY,
    @SerialName("Ср") WEDNESDAY,
    @SerialName("Чт") THURSDAY,
    @SerialName("Пт") FRIDAY,
    @SerialName("Сб") SATURDAY,
    @SerialName("Вс") SUNDAY;
}
/*
fun java.time.DayOfWeek.castToSerializable(): DayOfWeek {
    return when(this) {
        java.time.DayOfWeek.MONDAY -> DayOfWeek.MONDAY
        java.time.DayOfWeek.TUESDAY -> DayOfWeek.TUESDAY
        java.time.DayOfWeek.WEDNESDAY -> DayOfWeek.WEDNESDAY
        java.time.DayOfWeek.THURSDAY -> DayOfWeek.THURSDAY
        java.time.DayOfWeek.FRIDAY -> DayOfWeek.FRIDAY
        java.time.DayOfWeek.SATURDAY -> DayOfWeek.SATURDAY
        java.time.DayOfWeek.SUNDAY -> DayOfWeek.SUNDAY
    }
}*/

fun kotlinx.datetime.DayOfWeek.castToSerializable(): DayOfWeek {
    return when(this) {
        kotlinx.datetime.DayOfWeek.MONDAY -> DayOfWeek.MONDAY
        kotlinx.datetime.DayOfWeek.TUESDAY -> DayOfWeek.TUESDAY
        kotlinx.datetime.DayOfWeek.WEDNESDAY -> DayOfWeek.WEDNESDAY
        kotlinx.datetime.DayOfWeek.THURSDAY -> DayOfWeek.THURSDAY
        kotlinx.datetime.DayOfWeek.FRIDAY -> DayOfWeek.FRIDAY
        kotlinx.datetime.DayOfWeek.SATURDAY -> DayOfWeek.SATURDAY
        kotlinx.datetime.DayOfWeek.SUNDAY -> DayOfWeek.SUNDAY

        else -> throw IllegalArgumentException("Unknown DayOfWeek: $this")
    }
}