package ru.lavafrai.maiapp.navigation.pages

import kotlinx.serialization.Serializable
import ru.lavafrai.maiapp.models.schedule.BaseScheduleId
import ru.lavafrai.maiapp.models.schedule.ScheduleId

@Serializable
data class DedicatedSchedulePage(
    val scheduleId: ScheduleId,
    val title: String? = null,
)