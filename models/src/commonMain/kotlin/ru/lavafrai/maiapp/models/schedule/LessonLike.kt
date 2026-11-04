package ru.lavafrai.maiapp.models.schedule

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import ru.lavafrai.maiapp.models.time.now

interface LessonLike {
    val name: String
    val startTime: LocalTime
    val endTime: LocalTime
    val date: LocalDate
    val type: LessonType
    val classrooms: List<String>

    fun getUid(): Int

    /**
     * Uid, который считался раньше — до того, как в него добавили преподавателей и аудитории.
     * Нужен, чтобы пометки, поставленные пользователем до обновления, не потерялись.
     */
    fun getLegacyUid(): Int = getUid()

    val startTimePaddedString: String
        get() = "${startTime.hour.toString().padStart(2, '0')}:${startTime.minute.toString().padStart(2, '0')}"
    val endTimePaddedString: String
        get() = "${endTime.hour.toString().padStart(2, '0')}:${endTime.minute.toString().padStart(2, '0')}"
    val timeRangePaddedText: String
        get() = "$startTimePaddedString – $endTimePaddedString"

    fun isFinished(): Boolean {
        if (date > LocalDate.now()) return false
        if (date < LocalDate.now()) return true
        return endTime < LocalTime.now()
    }
}

/** Относится ли пометка с этим uid к занятию: по новому uid или по старому, до обновления */
fun LessonLike.matchesUid(uid: Int): Boolean = uid == getUid() || uid == getLegacyUid()
