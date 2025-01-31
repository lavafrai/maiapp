package ru.lavafrai.maiapp.models.schedule

import kotlinx.serialization.Serializable
import ru.lavafrai.maiapp.models.StringSerializer

@Serializable(with = TeacherNameSerializer::class)
class TeacherName(val name: String): ScheduleId() {
    init {
        // require(name.isNotBlank())
        // require(isValid(name))
    }

    override val scheduleId: String
        get() = name

    fun toAbstractScheduleId() = BaseScheduleId(name)

    companion object {
        private val nameRegex = "^([\\SА-Яа-яЁё-]+?( |\$)){3,5}\$".toRegex()
        val isValid: (String) -> Boolean = { nameRegex.matches(it) }
    }
}

class TeacherNameSerializer: StringSerializer<TeacherName>() {
    override fun deserialize(data: String): TeacherName = TeacherName(data)
    override fun serialize(data: TeacherName): String = data.name
}