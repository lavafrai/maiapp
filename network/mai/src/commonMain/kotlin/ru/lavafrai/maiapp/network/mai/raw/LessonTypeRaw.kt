package ru.lavafrai.maiapp.network.mai.raw

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class LessonTypeRaw {
    @SerialName("ЛК") LECTURE,
    @SerialName("ЛР") LABORATORY,
    @SerialName("ПЗ") SEMINAR,
    @SerialName("Экзамен") EXAM,
    @SerialName("Встреча") MEETING,
    @SerialName("Иное") OTHER2,
    @SerialName("") OTHER,
    ;

    fun toLessonType() = when(this) {
        LECTURE -> ru.lavafrai.maiapp.models.schedule.LessonType.LECTURE
        LABORATORY -> ru.lavafrai.maiapp.models.schedule.LessonType.LABORATORY
        SEMINAR -> ru.lavafrai.maiapp.models.schedule.LessonType.SEMINAR
        EXAM -> ru.lavafrai.maiapp.models.schedule.LessonType.EXAM
        MEETING -> ru.lavafrai.maiapp.models.schedule.LessonType.MEETING
        OTHER, OTHER2 -> ru.lavafrai.maiapp.models.schedule.LessonType.OTHER
    }
}