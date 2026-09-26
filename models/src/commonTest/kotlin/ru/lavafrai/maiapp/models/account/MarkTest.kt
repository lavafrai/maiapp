package ru.lavafrai.maiapp.models.account

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MarkTest {
    @Test
    fun failedMarksAreDebts() {
        listOf("Нзч", "Ня", "2", "НК", "НД").forEach { assertTrue(mark(it).isDebt, it) }
    }

    @Test
    fun debtMarksAreCaseInsensitive() {
        listOf("Нк", "нк", "Нд", "нд", "НЗЧ", " НК ").forEach { assertTrue(mark(it).isDebt, "'$it'") }
    }

    @Test
    fun passedAndMissingMarksAreNotDebts() {
        listOf("Зч", "3", "4", "5", "").forEach { assertFalse(mark(it).isDebt, "'$it'") }
    }

    private fun mark(value: String) = Mark(
        name = "Математика",
        value = value,
        attempts = 1,
        typeControlName = "Экзамен",
        semester = 1,
        course = 1,
        hours = 144,
        lecturer = "",
    )
}
