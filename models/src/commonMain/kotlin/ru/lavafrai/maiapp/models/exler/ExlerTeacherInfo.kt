package ru.lavafrai.maiapp.models.exler

import kotlinx.serialization.Serializable

@Serializable
data class ExlerTeacherInfo (
    val name: String,
    val link: String,
    val faculty: String?,
    val department: String?,
    val photo: List<String>?,
    val reviews: List<ExlerTeacherReview>,
)