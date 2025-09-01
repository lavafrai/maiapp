package ru.lavafrai.maiapp.navigation.pages

import kotlinx.serialization.Serializable
import ru.lavafrai.maiapp.models.schedule.ScheduleId

@Serializable
class EventEditorPage(
    val scheduleId: ScheduleId,
)