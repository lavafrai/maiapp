package ru.lavafrai.maiapp.navigation.pages

import kotlinx.serialization.Serializable
import ru.lavafrai.maiapp.models.schedule.TeacherUid

@Serializable
data class TeacherReviewsPage(
    val teacherId: String,
    val teacherUid: TeacherUid,
)