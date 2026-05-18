package ru.lavafrai.maiapp.navigation.pages

import kotlinx.serialization.Serializable
import ru.lavafrai.maiapp.models.events.RenderedEvent
import ru.lavafrai.maiapp.models.schedule.Schedule

@Serializable
data class EventDetailsPage(
    val schedule: Schedule,
    val event: RenderedEvent,
)