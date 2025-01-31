package ru.lavafrai.maiapp.models.schedule

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@JvmInline
@Serializable
value class AbstractScheduleId(val id: String): ScheduleId {
    override val scheduleId: String
        get() = id

    override fun toString(): String = id
}