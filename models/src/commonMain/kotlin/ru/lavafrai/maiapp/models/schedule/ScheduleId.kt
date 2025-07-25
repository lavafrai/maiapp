package ru.lavafrai.maiapp.models.schedule

import kotlinx.serialization.Serializable
import ru.lavafrai.maiapp.models.StringSerializer

@Serializable//(with = ScheduleIdSerializer::class)
sealed class ScheduleId {
    abstract val scheduleId: String

    override fun equals(other: Any?): Boolean = other is ScheduleId && scheduleId == other.scheduleId
    override fun hashCode(): Int {
        return scheduleId.hashCode()
    }

    fun toBase() = BaseScheduleId(scheduleId)
}

class ScheduleIdSerializer: StringSerializer<ScheduleId>() {
    override fun deserialize(data: String): ScheduleId = BaseScheduleId(data)
    override fun serialize(data: ScheduleId): String = data.scheduleId
}