@file:OptIn(ExperimentalUuidApi::class)

package ru.lavafrai.maiapp.models.events

import kotlinx.serialization.Serializable
import ru.lavafrai.maiapp.models.time.DateRange
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


@Serializable
sealed class Event {
    abstract val uid: Uuid
    abstract fun renderForDateRange(dateRange: DateRange?): List<RenderedEvent>
}