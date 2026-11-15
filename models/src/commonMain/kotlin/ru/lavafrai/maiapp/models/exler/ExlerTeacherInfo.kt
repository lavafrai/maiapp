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
    // Все места работы: у преподавателя их бывает несколько
    val places: List<ExlerTeacherPlace> = emptyList(),
    val photos: List<String>,
    val largePhotos: Map<String, String>,
    val reviews: List<ExlerTeacherReview>,
)

@Serializable
data class ExlerTeacherPlace(
    val faculty: String? = null,
    val department: String? = null,
    val unit: String? = null,
)