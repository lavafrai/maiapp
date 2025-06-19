@file:OptIn(ExperimentalUuidApi::class)

package ru.lavafrai.maiapp.models.events

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


data class RenderedEvent(
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val name: String,
    val teachers: List<String>,
    val room: String?,
    val type: EventType,
    val uuid: Uuid, // identity of event that produced this RenderedEvent, e.g. SingleEvent.uuid or GroupEvent.uuid
)