package ru.lavafrai.maiapp.models.exler

import kotlinx.serialization.Serializable

@Serializable
data class ExlerTeacherInfo (
    val name: String,
    val link: String,
    val faculty: String?,
    val department: String?,
    val photos: List<String>,
    val largePhotos: Map<String, String>,
    val reviews: List<ExlerTeacherReview>,
)