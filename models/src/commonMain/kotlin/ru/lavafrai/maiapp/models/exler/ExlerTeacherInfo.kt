package ru.lavafrai.maiapp.models.exler

import kotlinx.serialization.Serializable

@Serializable
data class ExlerTeacherInfo (
    val name: String,
    val link: String,
    val faculty: String?,
    val department: String?,
    // Подразделение вместо факультета: так на сайте подписаны школы и предуниверсарий
    val unit: String? = null,
    val photos: List<String>,
    val largePhotos: Map<String, String>,
    val reviews: List<ExlerTeacherReview>,
)