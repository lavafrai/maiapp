package ru.lavafrai.maiapp.models.schedule

import kotlinx.serialization.Serializable
import ru.lavafrai.maiapp.models.StringSerializer

@Serializable(with = ScheduleIdSerializer::class)
abstract class ScheduleId {
    abstract val scheduleId: String
}

class ScheduleIdSerializer: StringSerializer<ScheduleId>() {
    override fun deserialize(data: String): ScheduleId = BaseScheduleId(data)
    override fun serialize(data: ScheduleId): String = data.scheduleId
}