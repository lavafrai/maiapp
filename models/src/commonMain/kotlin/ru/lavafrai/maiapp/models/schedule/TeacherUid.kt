package ru.lavafrai.maiapp.models.schedule

import kotlinx.serialization.Serializable
import ru.lavafrai.maiapp.models.StringSerializer

@Serializable(with = TeacherUidSerializer::class)
class TeacherUid(val uid: String): ScheduleId() {
    init {
        require(uid.isNotBlank())
        // require(isValid(uid))
    }

    override val scheduleId: String
        get() = uid

    companion object {
        private val uidRegex = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\$".toRegex()
        val isValid: (String) -> Boolean = { uidRegex.matches(it) }
        val Empty: TeacherUid = TeacherUid("00000000-0000-0000-0000-000000000000")
    }
}


class TeacherUidSerializer: StringSerializer<TeacherUid>() {
    override fun deserialize(data: String): TeacherUid = TeacherUid(data)
    override fun serialize(data: TeacherUid): String = data.uid
}