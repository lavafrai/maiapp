package ru.lavafrai.maiapp.models.events

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import ru.lavafrai.maiapp.models.schedule.LessonType
import ru.lavafrai.maiapp.models.time.DateRange
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.uuid.Uuid

class SimpleEventTest {
    @Test
    fun singleIsRenderedOnlyInsideRange() {
        val event = event(LocalDate(2026, 9, 23), null, SimpleEventPeriod.Single)
        assertEquals(listOf(LocalDate(2026, 9, 23)), event.dates(null))
        assertEquals(listOf(LocalDate(2026, 9, 23)), event.dates(september(21, 27)))
        assertEquals(emptyList(), event.dates(september(24, 30)))
    }

    @Test
    fun weeklyWithoutRangeCoversWholePeriod() {
        val event = event(LocalDate(2026, 9, 2), LocalDate(2026, 9, 30), SimpleEventPeriod.Weekly)
        assertEquals(listOf(2, 9, 16, 23, 30).map { LocalDate(2026, 9, it) }, event.dates(null))
    }

    @Test
    fun biweeklyCountsFromStartDate() {
        val event = event(LocalDate(2026, 9, 2), LocalDate(2026, 9, 30), SimpleEventPeriod.Biweekly)
        assertEquals(listOf(2, 16, 30).map { LocalDate(2026, 9, it) }, event.dates(null))
    }

    @Test
    fun weeklyIsClippedByRange() {
        val event = event(LocalDate(2026, 9, 2), LocalDate(2026, 9, 30), SimpleEventPeriod.Weekly)
        assertEquals(listOf(16, 23).map { LocalDate(2026, 9, it) }, event.dates(september(10, 24)))
    }

    @Test
    fun rangeOutsideOfPeriodRendersNothing() {
        val event = event(LocalDate(2026, 9, 9), LocalDate(2026, 9, 23), SimpleEventPeriod.Weekly)
        assertEquals(emptyList(), event.dates(september(1, 7)))
        assertEquals(emptyList(), event.dates(september(24, 30)))
    }

    @Test
    fun monthlyRepeatsOnSameDayOfMonth() {
        val event = event(LocalDate(2026, 1, 15), LocalDate(2026, 4, 15), SimpleEventPeriod.Monthly)
        assertEquals(listOf(1, 2, 3, 4).map { LocalDate(2026, it, 15) }, event.dates(null))
    }

    @Test
    fun monthlySkipsMonthsWithoutThatDay() {
        val event = event(LocalDate(2026, 1, 31), LocalDate(2026, 5, 31), SimpleEventPeriod.Monthly)
        assertEquals(listOf(1, 3, 5).map { LocalDate(2026, it, 31) }, event.dates(null))
    }

    private fun september(start: Int, end: Int) = DateRange(LocalDate(2026, 9, start), LocalDate(2026, 9, end))

    private fun SimpleEvent.dates(range: DateRange?) = renderForDateRange(range).map { it.date }

    private fun event(date: LocalDate, endDate: LocalDate?, period: SimpleEventPeriod) = SimpleEvent(
        name = "Event",
        date = date,
        endDate = endDate,
        startTime = LocalTime(12, 0),
        endTime = LocalTime(13, 0),
        room = emptyList(),
        teachers = emptyList(),
        eventType = LessonType.OTHER,
        period = period,
        _uuid = Uuid.random(),
    )
}
