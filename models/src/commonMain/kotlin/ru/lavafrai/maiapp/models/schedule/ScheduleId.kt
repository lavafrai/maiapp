package ru.lavafrai.maiapp.models.schedule

import kotlinx.serialization.Serializable

@Serializable
sealed interface ScheduleId {
    val scheduleId: String
}