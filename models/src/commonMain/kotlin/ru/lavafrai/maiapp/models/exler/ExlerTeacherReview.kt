package ru.lavafrai.maiapp.models.exler

import kotlinx.serialization.Serializable

@Serializable
class ExlerTeacherReview(
        val author: String?,
        val source: String?,
        val publishTime: String?,
        val hypertext: String?,
)