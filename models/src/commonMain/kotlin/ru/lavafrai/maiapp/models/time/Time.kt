package ru.lavafrai.maiapp.models.time

import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable


@Serializable
class Time (
    val time: String
) {
    fun toLocalTime(): kotlinx.datetime.LocalTime {
        val hours = time.split(":")[0].toInt()
        val minutes = time.split(":")[1].toInt()
        val seconds = time.split(":")[2].toInt()

        return LocalTime(hours, minutes, seconds)
    }
}