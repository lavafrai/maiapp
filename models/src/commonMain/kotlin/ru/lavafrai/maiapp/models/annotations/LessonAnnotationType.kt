package ru.lavafrai.maiapp.models.annotations

import kotlinx.serialization.Serializable


@Serializable
data class LessonAnnotationType (
    val name: String,
    val priority: Long,
    val hasUserData: Boolean = false
)