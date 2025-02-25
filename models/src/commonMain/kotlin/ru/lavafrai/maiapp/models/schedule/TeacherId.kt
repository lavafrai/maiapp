package ru.lavafrai.maiapp.models.schedule

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.lavafrai.maiapp.models.Nameable

@Serializable
data class TeacherId(
    @SerialName("name") val teacherName: TeacherName,
    @SerialName("uid") val uid: TeacherUid,
): Nameable, ScheduleId() {
    override val name: String
        get() = teacherName.name

    override val scheduleId: String
        get() = teacherName.name
}