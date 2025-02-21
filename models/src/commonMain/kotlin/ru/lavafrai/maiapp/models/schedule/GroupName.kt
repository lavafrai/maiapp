package ru.lavafrai.maiapp.models.schedule

import kotlinx.serialization.Serializable
import ru.lavafrai.maiapp.models.StringSerializer

@Serializable//(with = GroupNameSerializer::class)
class GroupName(val name: String): ScheduleId() {
    override val scheduleId: String
        get() = name

    init {
        require(isValid(name))
    }

    fun toAbstractScheduleId() = BaseScheduleId(name)

    companion object {
        private val groupRegex = "^(([МТ])([\\dИУ]+?)([ОВЗ]))-((\\d+?)(Б|С|А|СВ|БВ|М)к?и?)-(\\d+?)$".toRegex()
        val isValid: (String) -> Boolean = { groupRegex.matches(it) }
    }
}

class GroupNameSerializer: StringSerializer<GroupName>() {
    override fun deserialize(data: String): GroupName = GroupName(data)
    override fun serialize(data: GroupName): String = data.name
}