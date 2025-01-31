package ru.lavafrai.maiapp.models.schedule

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@JvmInline
@Serializable
value class TeacherName(val name: String): ScheduleId {
    init {
        // require(name.isNotBlank())
        // require(isValid(name))
    }

    override val scheduleId: String
        get() = name

    fun toAbstractScheduleId() = AbstractScheduleId(name)

    companion object {
        private val nameRegex = "^([\\SА-Яа-яЁё-]+?( |\$)){3,5}\$".toRegex()
        val isValid: (String) -> Boolean = { nameRegex.matches(it) }
    }
}