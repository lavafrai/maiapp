package ru.lavafrai.maiapp.models.schedule

import kotlinx.serialization.Serializable
import ru.lavafrai.maiapp.models.StringSerializer

@Serializable//(with = BaseScheduleIdSerializer::class)
data class BaseScheduleId(val id: String): ScheduleId() {
    override val scheduleId: String
        get() = id

    override fun toString(): String = id
}

class BaseScheduleIdSerializer: StringSerializer<BaseScheduleId>() {
    override fun deserialize(data: String): BaseScheduleId = BaseScheduleId(data)
    override fun serialize(data: BaseScheduleId): String = data.id
}

