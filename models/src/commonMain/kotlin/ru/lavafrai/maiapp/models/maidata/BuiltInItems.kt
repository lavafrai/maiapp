package ru.lavafrai.maiapp.models.maidata

import kotlinx.serialization.SerialName

enum class BuiltInItems {
    @SerialName("teachers-reviews") TeachersReviews,
    @SerialName("teachers-schedules") TeachersSchedules,
}