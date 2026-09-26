package ru.lavafrai.maiapp.models.schedule

import kotlinx.datetime.LocalDate
import ru.lavafrai.maiapp.models.events.Event
import ru.lavafrai.maiapp.models.time.DateRange
import ru.lavafrai.maiapp.models.time.now
import ru.lavafrai.maiapp.models.time.week

/**
 * Week that should be opened by default.
 *
 * The current week is opened while there is at least one lesson or event today (even an already finished one)
 * or later this week. Otherwise the current week is over and the next one is opened, but only if it has
 * something in it; an empty next week is no better than the current one.
 *
 * @param filter lessons and events rejected by it are ignored (e.g. hidden military training)
 */
fun Schedule.defaultWeek(
    events: List<Event>,
    today: LocalDate = LocalDate.now(),
    filter: (LessonLike) -> Boolean = { true },
): DateRange {
    val currentWeek = today.week()
    val nextWeek = currentWeek.plusDays(7)

    fun hasSomethingIn(range: DateRange): Boolean {
        val lessons = days.asSequence()
            .filter { it.date in range }
            .flatMap { it.lessons }
        val renderedEvents = events.asSequence()
            .flatMap { it.renderForDateRange(range) }
            .filter { it.date in range }
        return (lessons + renderedEvents).any(filter)
    }

    if (hasSomethingIn(DateRange(today, currentWeek.endDate))) return currentWeek
    return if (hasSomethingIn(nextWeek)) nextWeek else currentWeek
}
