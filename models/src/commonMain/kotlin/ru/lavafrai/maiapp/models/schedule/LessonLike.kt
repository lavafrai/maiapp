package ru.lavafrai.maiapp.models.schedule

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

interface LessonLike {
    val name: String
    val startTime: LocalTime
    val endTime: LocalTime
    val date: LocalDate
    val type: LessonType
    val classrooms: List<String>

    fun getUid(): Int

    val startTimePaddedString: String
        get() = "${startTime.hour.toString().padStart(2, '0')}:${startTime.minute.toString().padStart(2, '0')}"
    val endTimePaddedString: String
        get() = "${endTime.hour.toString().padStart(2, '0')}:${endTime.minute.toString().padStart(2, '0')}"
    val timeRangePaddedText: String
        get() = "$startTimePaddedString – $endTimePaddedString"
}