package ru.lavafrai.maiapp.navigation.pages

import kotlinx.serialization.Serializable
import ru.lavafrai.maiapp.models.schedule.Lesson
import ru.lavafrai.maiapp.models.schedule.Schedule

@Serializable
data class LessonDetailsPage(
    val schedule: Schedule,
    val lesson: Lesson,
)