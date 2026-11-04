package ru.lavafrai.maiapp.models.exler

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class TeacherMatchTest {
    private val faculty = ExlerFaculty(name = "Факультет № 1", path = "/prepods/01/")

    private fun teacher(name: String, path: String = "/prepods/01/sidorov/") =
        ExlerTeacher(internalName = name, path = path, faculty = faculty)

    @Test
    fun letterYoDoesNotMatter() {
        assertEquals(normalizeTeacherName("Сидоров Пётр Иванович"), normalizeTeacherName("Сидоров Петр Иванович"))
    }

    @Test
    fun caseDoesNotMatter() {
        assertEquals(normalizeTeacherName("Сидоров Пётр Иванович"), normalizeTeacherName("сидоров пётр иванович"))
    }

    @Test
    fun wordOrderDoesNotMatter() {
        assertEquals(normalizeTeacherName("Сидоров Пётр Иванович"), normalizeTeacherName("Пётр Иванович Сидоров"))
    }

    @Test
    fun extraSpacesDoNotMatter() {
        assertEquals(normalizeTeacherName("Сидоров Пётр Иванович"), normalizeTeacherName("  Сидоров  Пётр Иванович "))
    }

    @Test
    fun findsTeacherWithAnotherLetterYo() {
        val teachers = listOf(teacher("Сидоров Петр Иванович"))
        assertNotNull(teachers.findByTeacherName("Сидоров Пётр Иванович"))
    }

    @Test
    fun returnsTeacherNameThatServerKnows() {
        val teachers = listOf(teacher("Сидоров Петр Иванович"))
        assertEquals("Сидоров Петр Иванович", teachers.findByTeacherName("Сидоров Пётр Иванович")?.name)
    }

    @Test
    fun findsTeacherAmongOthers() {
        val teachers = listOf(
            teacher("Иванов Иван Иванович", "/prepods/01/ivanov/"),
            teacher("Сидоров Петр Иванович", "/prepods/02/sidorov/"),
        )
        assertEquals("/prepods/02/sidorov/", teachers.findByTeacherName("Сидоров Пётр Иванович")?.path)
    }

    @Test
    fun returnsNullForTeacherWithoutReviews() {
        val teachers = listOf(teacher("Сидоров Петр Иванович"))
        assertNull(teachers.findByTeacherName("Петров Пётр Петрович"))
    }

    @Test
    fun returnsNullForEmptyList() {
        assertNull(emptyList<ExlerTeacher>().findByTeacherName("Сидоров Пётр Иванович"))
    }

    @Test
    fun nameHashIgnoresYoAndWordOrder() {
        assertEquals(teacherNameHash("Сидоров Пётр Иванович"), teacherNameHash("Петр Иванович Сидоров"))
    }
}
