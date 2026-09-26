package ru.lavafrai.maiapp.models.time

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DateTest {
    @Test
    fun weekStartsOnMondayAndEndsOnSunday() {
        val week = DateRange(LocalDate(2026, 9, 21), LocalDate(2026, 9, 27))
        assertEquals(week, LocalDate(2026, 9, 21).week())
        assertEquals(week, LocalDate(2026, 9, 24).week())
        assertEquals(week, LocalDate(2026, 9, 27).week())
    }

    @Test
    fun weekCrossesMonthAndYear() {
        assertEquals(
            DateRange(LocalDate(2026, 12, 28), LocalDate(2027, 1, 3)),
            LocalDate(2027, 1, 1).week(),
        )
    }

    @Test
    fun russianFormatIsZeroPadded() {
        assertEquals("03.09.2026", LocalDate(2026, 9, 3).toRussianFormatString())
    }

    @Test
    fun russianFormatParsesMaiDates() {
        assertEquals(LocalDate(2026, 9, 3), LocalDate.parse("03.09.2026", RussianDateFormat))
    }

    @Test
    fun russianFormatRejectsInvalidDates() {
        assertFailsWith<IllegalArgumentException> { LocalDate.parse("31.02.2026", RussianDateFormat) }
        assertFailsWith<IllegalArgumentException> { LocalDate.parse("2026-09-03", RussianDateFormat) }
    }
}
