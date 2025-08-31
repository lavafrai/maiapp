package ru.lavafrai.maiapp.models.schedule

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
enum class LessonType {
    @SerialName("ЛК") LECTURE,
    @SerialName("ЛР") LABORATORY,
    @SerialName("ПЗ") SEMINAR,
    @SerialName("Экзамен") EXAM,
    @SerialName("Встреча") MEETING,
    @SerialName("") OTHER,
    ;
}