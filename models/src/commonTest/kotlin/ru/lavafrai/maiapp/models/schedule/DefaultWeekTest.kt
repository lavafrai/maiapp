package ru.lavafrai.maiapp.models.schedule

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import ru.lavafrai.maiapp.models.events.Event
import ru.lavafrai.maiapp.models.events.SimpleEvent
import ru.lavafrai.maiapp.models.events.SimpleEventPeriod
import ru.lavafrai.maiapp.models.time.DateRange
import ru.lavafrai.maiapp.models.time.Time
import ru.lavafrai.maiapp.models.time.castToSerializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.uuid.Uuid

class DefaultWeekTest {
    // Week of 2026-09-21 (Mon) .. 2026-09-27 (Sun)
    private val monday = LocalDate(2026, 9, 21)
    private val tuesday = LocalDate(2026, 9, 22)
    private val wednesday = LocalDate(2026, 9, 23)
    private val thursday = LocalDate(2026, 9, 24)
    private val friday = LocalDate(2026, 9, 25)
    private val saturday = LocalDate(2026, 9, 26)
    private val sunday = LocalDate(2026, 9, 27)
    private val previousSunday = LocalDate(2026, 9, 20)
    private val nextMonday = LocalDate(2026, 9, 28)
    private val nextWednesday = LocalDate(2026, 9, 30)
    private val weekAfterNextMonday = LocalDate(2026, 10, 5)

    private val currentWeek = DateRange(monday, sunday)
    private val nextWeek = DateRange(nextMonday, LocalDate(2026, 10, 4))

    // region Schedule lessons

    @Test
    fun emptyScheduleKeepsCurrentWeek() {
        assertEquals(currentWeek, schedule().defaultWeek(emptyList(), today = wednesday))
    }

    @Test
    fun lessonTodayKeepsCurrentWeek() {
        assertEquals(currentWeek, schedule(wednesday).defaultWeek(emptyList(), today = wednesday))
    }

    @Test
    fun lessonTodayKeepsCurrentWeekEvenIfItIsLastLessonOfWeek() {
        val schedule = schedule(monday, tuesday, wednesday)
        assertEquals(currentWeek, schedule.defaultWeek(emptyList(), today = wednesday))
    }

    @Test
    fun lessonLaterThisWeekKeepsCurrentWeek() {
        assertEquals(currentWeek, schedule(saturday).defaultWeek(emptyList(), today = wednesday))
    }

    @Test
    fun lessonOnSundayKeepsCurrentWeek() {
        assertEquals(currentWeek, schedule(sunday).defaultWeek(emptyList(), today = wednesday))
    }

    @Test
    fun onlyPastLessonsThisWeekOpenNextWeek() {
        val schedule = schedule(monday, tuesday, nextMonday)
        assertEquals(nextWeek, schedule.defaultWeek(emptyList(), today = wednesday))
    }

    @Test
    fun onlyNextWeekLessonsOpenNextWeek() {
        assertEquals(nextWeek, schedule(nextMonday).defaultWeek(emptyList(), today = saturday))
    }

    @Test
    fun lessonsInPreviousAndNextWeekOpenNextWeek() {
        assertEquals(nextWeek, schedule(previousSunday, nextWednesday).defaultWeek(emptyList(), today = monday))
    }

    @Test
    fun lessonsOnlyInPreviousWeekKeepCurrentWeek() {
        assertEquals(currentWeek, schedule(previousSunday).defaultWeek(emptyList(), today = monday))
    }

    @Test
    fun weekIsOverAndNextWeekIsEmptyKeepsCurrentWeek() {
        assertEquals(currentWeek, schedule(monday, tuesday).defaultWeek(emptyList(), today = wednesday))
    }

    @Test
    fun weekIsOverAndOnlyWeekAfterNextHasLessonsKeepsCurrentWeek() {
        assertEquals(currentWeek, schedule(monday, weekAfterNextMonday).defaultWeek(emptyList(), today = wednesday))
    }

    @Test
    fun lessonOnLastDayOfNextWeekOpensNextWeek() {
        assertEquals(nextWeek, schedule(monday, nextWeek.endDate).defaultWeek(emptyList(), today = wednesday))
    }

    @Test
    fun dayWithoutLessonsDoesNotCount() {
        val schedule = scheduleOf(
            ScheduleDay(friday, friday.dayOfWeek.castToSerializable(), emptyList()),
            day(nextWednesday, lesson(nextWednesday)),
        )
        assertEquals(nextWeek, schedule.defaultWeek(emptyList(), today = wednesday))
    }

    @Test
    fun mondayWithLessonTodayKeepsCurrentWeek() {
        assertEquals(currentWeek, schedule(monday).defaultWeek(emptyList(), today = monday))
    }

    @Test
    fun mondayWithoutLessonsThisWeekOpensNextWeek() {
        assertEquals(nextWeek, schedule(nextMonday).defaultWeek(emptyList(), today = monday))
    }

    @Test
    fun sundayWithLessonTodayKeepsCurrentWeek() {
        assertEquals(currentWeek, schedule(friday, sunday).defaultWeek(emptyList(), today = sunday))
    }

    @Test
    fun sundayWithoutLessonsTodayOpensNextWeek() {
        assertEquals(nextWeek, schedule(friday, saturday, nextMonday).defaultWeek(emptyList(), today = sunday))
    }

    @Test
    fun everyDayOfWeekBehavesConsistently() {
        // Lessons on Tuesday and Thursday, then on next Wednesday
        val schedule = schedule(tuesday, thursday, nextWednesday)
        val expected = mapOf(
            monday to currentWeek,
            tuesday to currentWeek,
            wednesday to currentWeek,
            thursday to currentWeek,
            friday to nextWeek,
            saturday to nextWeek,
            sunday to nextWeek,
        )
        expected.forEach { (today, week) ->
            assertEquals(week, schedule.defaultWeek(emptyList(), today = today), "today = $today")
        }
    }

    @Test
    fun weekCrossingYearBoundary() {
        val thursday = LocalDate(2026, 12, 31)
        val weekOfNewYear = DateRange(LocalDate(2026, 12, 28), LocalDate(2027, 1, 3))
        val firstWeekOfYear = DateRange(LocalDate(2027, 1, 4), LocalDate(2027, 1, 10))

        assertEquals(weekOfNewYear, schedule(LocalDate(2027, 1, 2)).defaultWeek(emptyList(), today = thursday))
        assertEquals(firstWeekOfYear, schedule(LocalDate(2026, 12, 29), LocalDate(2027, 1, 5)).defaultWeek(emptyList(), today = thursday))
        assertEquals(weekOfNewYear, schedule(LocalDate(2026, 12, 29)).defaultWeek(emptyList(), today = thursday))
    }

    // endregion

    // region User events

    @Test
    fun singleEventTodayKeepsCurrentWeek() {
        val events = listOf(event(saturday))
        assertEquals(currentWeek, schedule(monday).defaultWeek(events, today = saturday))
    }

    @Test
    fun singleEventLaterThisWeekKeepsCurrentWeek() {
        val events = listOf(event(sunday))
        assertEquals(currentWeek, schedule(monday).defaultWeek(events, today = saturday))
    }

    @Test
    fun singleEventEarlierThisWeekOpensNextWeek() {
        val events = listOf(event(friday))
        assertEquals(nextWeek, schedule(nextWednesday).defaultWeek(events, today = saturday))
        assertEquals(currentWeek, schedule().defaultWeek(events, today = saturday))
    }

    @Test
    fun singleEventNextWeekOpensNextWeek() {
        val events = listOf(event(nextMonday))
        assertEquals(nextWeek, schedule().defaultWeek(events, today = saturday))
    }

    @Test
    fun eventsWithoutScheduleLessonsAreEnough() {
        val events = listOf(event(sunday))
        assertEquals(currentWeek, schedule().defaultWeek(events, today = friday))
    }

    @Test
    fun weeklyEventRecurringLaterThisWeekKeepsCurrentWeek() {
        // Started on a Sunday a month ago, recurs every Sunday
        val events = listOf(event(LocalDate(2026, 8, 30), LocalDate(2026, 12, 31), SimpleEventPeriod.Weekly))
        assertEquals(currentWeek, schedule().defaultWeek(events, today = saturday))
    }

    @Test
    fun weeklyEventRecurringOnlyEarlierThisWeekOpensNextWeek() {
        // Recurs every Monday
        val events = listOf(event(LocalDate(2026, 8, 31), LocalDate(2026, 12, 31), SimpleEventPeriod.Weekly))
        assertEquals(nextWeek, schedule().defaultWeek(events, today = saturday))
    }

    @Test
    fun weeklyEventThatAlreadyEndedOpensNextWeek() {
        // Every Sunday, but ended before this Sunday
        val events = listOf(event(LocalDate(2026, 8, 30), LocalDate(2026, 9, 20), SimpleEventPeriod.Weekly))
        assertEquals(nextWeek, schedule(nextWednesday).defaultWeek(events, today = saturday))
        assertEquals(currentWeek, schedule().defaultWeek(events, today = saturday))
    }

    @Test
    fun weeklyEventStartingNextWeekOpensNextWeek() {
        val events = listOf(event(nextMonday, LocalDate(2026, 12, 31), SimpleEventPeriod.Weekly))
        assertEquals(nextWeek, schedule().defaultWeek(events, today = saturday))
    }

    @Test
    fun biweeklyEventOnThisWeekKeepsCurrentWeek() {
        // Sundays 2026-09-13, 2026-09-27, ...
        val events = listOf(event(LocalDate(2026, 9, 13), LocalDate(2026, 12, 31), SimpleEventPeriod.Biweekly))
        assertEquals(currentWeek, schedule().defaultWeek(events, today = saturday))
    }

    @Test
    fun weeklyEventEndingThisWeekKeepsCurrentWeekWhenNothingElseIsNext() {
        // Every Monday, last occurrence this Monday
        val events = listOf(event(LocalDate(2026, 8, 31), monday, SimpleEventPeriod.Weekly))
        assertEquals(currentWeek, schedule().defaultWeek(events, today = saturday))
    }

    @Test
    fun biweeklyEventOnOtherWeekOpensNextWeek() {
        // Sundays 2026-09-20, 2026-10-04, ...
        val events = listOf(event(LocalDate(2026, 9, 6), LocalDate(2026, 12, 31), SimpleEventPeriod.Biweekly))
        assertEquals(nextWeek, schedule().defaultWeek(events, today = saturday))
    }

    @Test
    fun monthlyEventLaterThisWeekKeepsCurrentWeek() {
        // Every 27th
        val events = listOf(event(LocalDate(2026, 7, 27), LocalDate(2026, 12, 31), SimpleEventPeriod.Monthly))
        assertEquals(currentWeek, schedule().defaultWeek(events, today = saturday))
    }

    @Test
    fun monthlyEventEarlierThisWeekOpensNextWeek() {
        // Every 25th, so nothing next week either
        val events = listOf(event(LocalDate(2026, 7, 25), LocalDate(2026, 12, 31), SimpleEventPeriod.Monthly))
        assertEquals(nextWeek, schedule(nextWednesday).defaultWeek(events, today = saturday))
        assertEquals(currentWeek, schedule().defaultWeek(events, today = saturday))
    }

    @Test
    fun monthlyEventNextWeekOpensNextWeek() {
        // Every 1st, next one is on next Thursday
        val events = listOf(event(LocalDate(2026, 7, 1), LocalDate(2026, 12, 31), SimpleEventPeriod.Monthly))
        assertEquals(nextWeek, schedule().defaultWeek(events, today = saturday))
    }

    // endregion

    // region Filter

    @Test
    fun filteredOutLessonsDoNotCount() {
        val schedule = scheduleOf(day(saturday, lesson(saturday, name = MILITARY)), day(nextWednesday, lesson(nextWednesday)))
        assertEquals(nextWeek, schedule.defaultWeek(emptyList(), today = saturday, filter = ::notMilitary))
    }

    @Test
    fun remainingLessonsCountWhenOthersAreFilteredOut() {
        val schedule = scheduleOf(day(saturday, lesson(saturday, name = MILITARY), lesson(saturday)))
        assertEquals(currentWeek, schedule.defaultWeek(emptyList(), today = saturday, filter = ::notMilitary))
    }

    @Test
    fun filterAppliesToEvents() {
        val events = listOf(event(sunday, name = MILITARY))
        assertEquals(nextWeek, schedule(nextWednesday).defaultWeek(events, today = saturday, filter = ::notMilitary))
        assertEquals(currentWeek, schedule(nextWednesday).defaultWeek(events, today = saturday))
    }

    @Test
    fun nextWeekWithOnlyFilteredOutLessonsKeepsCurrentWeek() {
        val schedule = scheduleOf(day(monday, lesson(monday)), day(nextWednesday, lesson(nextWednesday, name = MILITARY)))
        assertEquals(currentWeek, schedule.defaultWeek(emptyList(), today = saturday, filter = ::notMilitary))
        assertEquals(nextWeek, schedule.defaultWeek(emptyList(), today = saturday))
    }

    // endregion

    private fun notMilitary(lesson: LessonLike) = lesson.name != MILITARY

    private fun schedule(vararg lessonDates: LocalDate) = scheduleOf(*lessonDates.map { day(it, lesson(it)) }.toTypedArray())

    private fun scheduleOf(vararg days: ScheduleDay) = Schedule(
        name = "test",
        id = BaseScheduleId("test"),
        created = 0,
        cached = 0,
        days = days.toList(),
    )

    private fun day(date: LocalDate, vararg lessons: Lesson) = ScheduleDay(date, date.dayOfWeek.castToSerializable(), lessons.toList())

    private fun lesson(date: LocalDate, name: String = "Математика") = Lesson(
        name = name,
        timeStart = Time("09:00:00"),
        timeEnd = Time("10:30:00"),
        lectors = emptyList(),
        type = LessonType.LECTURE,
        day = date,
        rooms = emptyList(),
        lms = "",
        teams = "",
        other = "",
    )

    private fun event(
        date: LocalDate,
        endDate: LocalDate? = null,
        period: SimpleEventPeriod = SimpleEventPeriod.Single,
        name: String = "Событие",
    ): Event = SimpleEvent(
        name = name,
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

    private companion object {
        const val MILITARY = "Военная подготовка"
    }
}
