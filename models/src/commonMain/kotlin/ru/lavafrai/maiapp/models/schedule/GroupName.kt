package ru.lavafrai.maiapp.models.schedule

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@JvmInline
@Serializable
value class GroupName(val name: String): ScheduleId {
    override val scheduleId: String
        get() = name

    init {
        require(isValid(name))
    }

    fun toAbstractScheduleId() = AbstractScheduleId(name)

    companion object {
        private val groupRegex = "^(([МТ])([\\dИУ]+?)([ОВЗ]))-((\\d+?)(Б|С|А|СВ|БВ|М)к?и?)-(\\d+?)$".toRegex()
        val isValid: (String) -> Boolean = { groupRegex.matches(it) }
    }
}