@file:OptIn(ExperimentalFoundationApi::class)

package ru.lavafrai.maiapp.fragments.schedule

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import kotlinx.datetime.LocalDate
import ru.lavafrai.maiapp.models.schedule.Lesson
import ru.lavafrai.maiapp.models.schedule.Schedule
import ru.lavafrai.maiapp.models.schedule.ScheduleDay
import ru.lavafrai.maiapp.models.time.DateRange
import ru.lavafrai.maiapp.models.time.now

@Composable
fun ScheduleView(
    schedule: Schedule,
    dateRange: DateRange? = null,
    modifier: Modifier = Modifier,
    selector: (ScheduleDay, Lesson) -> Boolean = { _, _ -> true },
) {
    val filteredDays = remember(dateRange) { schedule.days.filter { if (dateRange == null) true else it.date!! in dateRange } }
    val filteredLessons = remember(dateRange, selector) {
        schedule.days.map { day -> day.copy(lessons=day.lessons.filter { selector(day, it) }) }.filter { it.lessons.isNotEmpty() }
    }
    val lazyColumnState = rememberLazyListState()

    LaunchedEffect(dateRange) {
        if (dateRange == null) {
            val index = filteredLessons.indexOfFirst { it.date!! >= LocalDate.now() }
            lazyColumnState.scrollToItem(index * 2)
            return@LaunchedEffect
        }
        if (dateRange.isNow()) lazyColumnState.scrollToItem((LocalDate.now().dayOfWeek.ordinal) * 2)
        else lazyColumnState.scrollToItem(0)
    }

    LazyColumn(
        modifier = modifier,
        state = lazyColumnState,
    ) {
        for (day in filteredLessons) {
            stickyHeader {
                DayHeader(day = day, modifier = Modifier.background(MaterialTheme.colorScheme.background))
            }
            item {
                DayView(day = day, modifier = Modifier)
            }
        }
    }
}