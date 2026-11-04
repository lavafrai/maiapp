package ru.lavafrai.maiapp.models.schedule

import kotlinx.datetime.LocalDate
import ru.lavafrai.maiapp.models.time.Time
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class LessonUidTest {
    private val date = LocalDate(2026, 9, 1)

    private fun lesson(
        teacherUid: String = "teacher-1",
        teacherName: String = "Иванов Иван Иванович",
        roomUid: String = "room-1",
        roomName: String = "ГУК 1",
    ) = Lesson(
        name = "Иностранный язык",
        timeStart = Time("9:00:00"),
        timeEnd = Time("10:30:00"),
        lectors = listOf(TeacherId(TeacherName(teacherName), TeacherUid(teacherUid))),
        type = LessonType.SEMINAR,
        day = date,
        rooms = listOf(Classroom(roomName, roomUid)),
        lms = "",
        teams = "",
        other = "",
    )

    @Test
    fun sameLessonHasSameUid() {
        assertEquals(lesson().getUid(), lesson().getUid())
    }

    @Test
    fun lessonsWithDifferentTeachersHaveDifferentUids() {
        assertNotEquals(
            lesson(teacherUid = "teacher-1", teacherName = "Иванов Иван Иванович").getUid(),
            lesson(teacherUid = "teacher-2", teacherName = "Петров Пётр Петрович").getUid(),
        )
    }

    @Test
    fun lessonsWithDifferentRoomsHaveDifferentUids() {
        assertNotEquals(
            lesson(roomUid = "room-1", roomName = "ГУК 1").getUid(),
            lesson(roomUid = "room-2", roomName = "ГУК 2").getUid(),
        )
    }

    @Test
    fun legacyUidIsTheSameForDifferentTeachers() {
        assertEquals(
            lesson(teacherUid = "teacher-1").getLegacyUid(),
            lesson(teacherUid = "teacher-2", teacherName = "Петров Пётр Петрович").getLegacyUid(),
        )
    }

    @Test
    fun legacyUidIsTheOldFormula() {
        assertEquals("Иностранный язык $date 1".hashCode(), lesson().getLegacyUid())
    }

    @Test
    fun matchesBothNewAndLegacyUid() {
        val lesson = lesson()

        assertEquals(true, lesson.matchesUid(lesson.getUid()))
        assertEquals(true, lesson.matchesUid(lesson.getLegacyUid()))
        assertEquals(false, lesson.matchesUid(lesson.getUid() + 1))
    }
}
