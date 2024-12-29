package ru.lavafrai.maiapp.navigation.pages

import kotlinx.serialization.Serializable
import ru.lavafrai.maiapp.models.schedule.Lesson

@Serializable
data class LessonDetailsPage(
    val lesson: Lesson
)