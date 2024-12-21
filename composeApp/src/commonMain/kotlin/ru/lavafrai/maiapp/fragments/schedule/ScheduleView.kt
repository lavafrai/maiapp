@file:OptIn(ExperimentalFoundationApi::class)

package ru.lavafrai.maiapp.fragments.schedule

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import ru.lavafrai.maiapp.fragments.PageColumn
import ru.lavafrai.maiapp.models.exler.ExlerTeacher
import ru.lavafrai.maiapp.models.schedule.Lesson
import ru.lavafrai.maiapp.models.schedule.Schedule
import ru.lavafrai.maiapp.models.schedule.ScheduleDay
import ru.lavafrai.maiapp.models.time.DateRange
import ru.lavafrai.maiapp.models.time.now

@Composable
fun ScheduleView(
    schedule: Schedule,
    dateRange: DateRange? = null,
    exlerTeachers: List<ExlerTeacher>? = null,
    modifier: Modifier = Modifier,
    selector: (ScheduleDay, Lesson) -> Boolean = { _, _ -> true },
) {
    val filteredDays = remember(dateRange) { schedule.days.filter { if (dateRange == null) true else it.date in dateRange } }
    val filteredLessons = remember(dateRange, selector) {
        filteredDays.map { day -> day.copy(lessons=day.lessons.filter { selector(day, it) }) }.filter { it.lessons.isNotEmpty() }
    }
    val lazyColumnState: LazyListState = rememberLazyListState()

    LaunchedEffect(dateRange) {
        if (dateRange == null || dateRange.isNow()) {
            val pairsFinishedToday = filteredLessons.find { it.date == LocalDate.now() }?.isFinished() ?: false
            var index = filteredLessons.indexOfFirst { it.date >= LocalDate.now() } + if (pairsFinishedToday) 1 else 0
            if (index == -1) index = filteredLessons.size - 1

            lazyColumnState.scrollToItem(index * 2)
            return@LaunchedEffect
        }

        else lazyColumnState.scrollToItem(0)
    }

    PageColumn(scrollState = null) {
        LazyColumn(
            modifier = modifier,
            state = lazyColumnState,
        ) {
            for (day in filteredLessons) {
                stickyHeader {
                    DayHeader(
                        day = day,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.background)
                            .padding(vertical = 8.dp),
                    )
                }

                item {
                    DayView(
                        day = day,
                        modifier = Modifier
                            .padding(vertical = 8.dp),
                        exlerTeachers = exlerTeachers,
                    )
                }
            }

            item {
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}