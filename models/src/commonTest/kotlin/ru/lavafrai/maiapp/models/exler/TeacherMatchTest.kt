package ru.lavafrai.maiapp.models.exler

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class TeacherMatchTest {
    private val faculty = ExlerFaculty(name = "Факультет № 1", path = "/prepods/01/")

    private fun teacher(name: String, path: String = "/prepods/01/elkin/") =
        ExlerTeacher(internalName = name, path = path, faculty = faculty)

    @Test
    fun letterYoDoesNotMatter() {
        assertEquals(normalizeTeacherName("Ёлкин Алексей Петрович"), normalizeTeacherName("Елкин Алексей Петрович"))
    }

    @Test
    fun caseDoesNotMatter() {
        assertEquals(normalizeTeacherName("Ёлкин Алексей Петрович"), normalizeTeacherName("ёлкин алексей петрович"))
    }

    @Test
    fun findsTeacherWrittenWithAnotherLetterYo() {
        val teachers = listOf(teacher("Елкин Алексей Петрович"))
        assertNotNull(teachers.findByTeacherName("Ёлкин Алексей Петрович"))
    }

    @Test
    fun findsTeacherWrittenWithoutYo() {
        val teachers = listOf(teacher("Ёлкин Алексей Петрович"))
        assertNotNull(teachers.findByTeacherName("Елкин Алексей Петрович"))
    }

    @Test
    fun findsTeacherIgnoringCase() {
        val teachers = listOf(teacher("Ёлкин Алексей Петрович"))
        assertEquals("Ёлкин Алексей Петрович", teachers.findByTeacherName("ёлкин алексей петрович")?.name)
    }

    @Test
    fun exactMatchHasPriority() {
        val teachers = listOf(
            teacher("Ёлкин Алексей Петрович", "/prepods/01/elkin-yo/"),
            teacher("Елкин Алексей Петрович", "/prepods/01/elkin-e/"),
        )

        assertEquals("/prepods/01/elkin-e/", teachers.findByTeacherName("Елкин Алексей Петрович")?.path)
        assertEquals("/prepods/01/elkin-yo/", teachers.findByTeacherName("Ёлкин Алексей Петрович")?.path)
    }

    @Test
    fun returnsNameAsInTheCatalog() {
        val teachers = listOf(teacher("Ёлкин Алексей Петрович"))
        assertEquals("Ёлкин Алексей Петрович", teachers.findByTeacherName("Елкин Алексей Петрович")?.name)
    }

    @Test
    fun returnsNullForTeacherWithoutReviews() {
        val teachers = listOf(teacher("Ёлкин Алексей Петрович"))
        assertNull(teachers.findByTeacherName("Петров Пётр Петрович"))
    }

    @Test
    fun returnsNullForEmptyList() {
        assertNull(emptyList<ExlerTeacher>().findByTeacherName("Ёлкин Алексей Петрович"))
    }

    @Test
    fun nameHashIgnoresYoAndCase() {
        assertEquals(teacherNameHash("Ёлкин Алексей Петрович"), teacherNameHash("ёлкин алексей петрович"))
    }
}
