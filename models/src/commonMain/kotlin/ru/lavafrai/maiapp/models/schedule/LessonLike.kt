package ru.lavafrai.maiapp.models.schedule

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

interface LessonLike {
    val name: String
    val startTime: LocalTime
    val endTime: LocalTime
    val date: LocalDate
    val type: LessonType
    fun getUid(): Int
}