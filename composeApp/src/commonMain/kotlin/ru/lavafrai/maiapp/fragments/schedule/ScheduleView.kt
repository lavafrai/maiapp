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
import ru.lavafrai.maiapp.models.schedule.Schedule
import ru.lavafrai.maiapp.models.time.DateRange
import ru.lavafrai.maiapp.models.time.now

@Composable
fun ScheduleView(
    schedule: Schedule,
    dateRange: DateRange? = null,
    modifier: Modifier = Modifier,
) {
    val filteredDays = remember(dateRange) { schedule.days.filter { if (dateRange == null) true else it.date!! in dateRange } }
    val lazyColumnState = rememberLazyListState()

    LaunchedEffect(dateRange) {
        if (dateRange == null) lazyColumnState.scrollToItem(0)
        if (dateRange!!.isNow()) lazyColumnState.scrollToItem((LocalDate.now().dayOfWeek.ordinal) * 2)
        else lazyColumnState.scrollToItem(0)
    }

    LazyColumn(
        modifier = modifier,
        state = lazyColumnState,
    ) {
        for (day in filteredDays) {
            stickyHeader {
                DayHeader(day = day, modifier = Modifier.background(MaterialTheme.colorScheme.background))
            }
            item {
                DayView(day = day, modifier = Modifier)
            }
        }
    }
}