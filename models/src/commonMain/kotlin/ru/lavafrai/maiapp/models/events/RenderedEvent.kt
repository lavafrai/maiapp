@file:OptIn(ExperimentalUuidApi::class)

package ru.lavafrai.maiapp.models.events

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable
import ru.lavafrai.maiapp.models.schedule.LessonLike
import ru.lavafrai.maiapp.models.schedule.LessonType
import ru.lavafrai.maiapp.models.time.zFill
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class RenderedEvent(
    override val name: String,
    override val startTime: LocalTime,
    override val endTime: LocalTime,
    override val date: LocalDate,
    val teachers: List<String>,
    override val classrooms: List<String>,
    override val type: LessonType,
    val uuid: Uuid, // identity of event that produced this RenderedEvent, e.g. SingleEvent.uuid or GroupEvent.uuid
): LessonLike {
    val timeRange: String
        get() = "${startTime.hour}:${startTime.minute.toString().zFill(2)} – ${endTime.hour}:${endTime.minute.toString().zFill(2)}"

    override fun getUid(): Int {
        return "event:$uuid:$date".hashCode()
    }
}