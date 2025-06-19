@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)

package ru.lavafrai.maiapp.fragments.schedule

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.LoadingIndicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import ru.lavafrai.maiapp.data.repositories.LessonAnnotationsRepository
import ru.lavafrai.maiapp.fragments.PageColumn
import ru.lavafrai.maiapp.models.exler.ExlerTeacher
import ru.lavafrai.maiapp.models.schedule.Schedule
import ru.lavafrai.maiapp.models.time.DateRange
import ru.lavafrai.maiapp.utils.LessonSelector
import kotlin.time.Duration.Companion.seconds


@Composable
fun ScheduleView(
    schedule: Schedule,
    dateRange: DateRange? = null,
    exlerTeachers: List<ExlerTeacher>? = null,
    modifier: Modifier = Modifier,
    selector: LessonSelector = LessonSelector.default(),
    onRefresh: (() -> Unit)? = null,
    refreshing: Boolean = false,
    state: ScheduleViewState = rememberScheduleViewState(),
    showEventAddingButton: Boolean = false,
    onAddEventClick: ((LocalDate) -> Unit) = {},
) {
    val lessonAnnotationRepository = remember { LessonAnnotationsRepository }
    val filteredDays =
        remember(dateRange, schedule) { schedule.days.filter { if (dateRange == null) true else it.date in dateRange } }
    val annotations by lessonAnnotationRepository.follow(schedule.name).collectAsState()

    val filteredLessons = remember(dateRange, selector, annotations, schedule, filteredDays) {
        filteredDays.map { day ->
            day.copy(lessons = day.lessons.filter { lesson ->
                selector.test(
                    day,
                    lesson,
                    lessonAnnotationRepository.loadAnnotations(schedule.name)
                        .filter { it.lessonUid == lesson.getUid() })
            })
        }.filter { it.lessons.isNotEmpty() }
    }
    val filteredAnnotations = remember(annotations, dateRange) {
        annotations.filter { it.lessonUid in filteredLessons.flatMap { it.lessons.map { it.getUid() } } }
    }

    LaunchedEffect(dateRange, selector, schedule.id, filteredLessons) {
        state.updateScrollIfRequired(schedule, dateRange, selector, filteredLessons)
    }

    val scope = rememberCoroutineScope()
    // var refreshing by remember { mutableStateOf(false) }
    val refresh = suspend {
        if (onRefresh != null) onRefresh()
        //delay(1.seconds)
        //refreshing = false
    }
    /*LaunchedEffect(schedule) {
        if (refreshing) {
            refreshing = false
        }
    }*/

    PageColumn(scrollState = null) {
        val content = @Composable {
            if (filteredLessons.isNotEmpty())
                LazyColumn(
                    modifier = modifier,
                    state = state.lazyScrollState,
                ) {
                    for (day in filteredLessons.sortedBy { it.date }) {
                        stickyHeader {
                            DayHeader(
                                day = day,
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.background)
                                    .padding(vertical = 8.dp),
                                showEventAddingButton = showEventAddingButton,
                                onAddEventClick = { onAddEventClick(day.date) },
                            )
                        }

                        item {
                            DayView(
                                day = day,
                                modifier = Modifier
                                    .padding(vertical = 8.dp),
                                exlerTeachers = exlerTeachers,
                                annotations = filteredAnnotations,
                                schedule = schedule,
                            )
                        }
                    }

                    item {
                        Spacer(Modifier.height(16.dp))
                    }
                } else {
                if (filteredDays.isEmpty()) EmptyScheduleView(
                    dateRange = dateRange,
                ) else if (filteredLessons.isEmpty()) NoLessonsFoundView(
                    dateRange = dateRange,
                )
            }
        }

        val hapticFeedback = LocalHapticFeedback.current
        val pullToRefreshState = rememberPullToRefreshState()

        if (onRefresh != null) PullToRefreshBox(
            onRefresh = {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                /*refreshing = true*/
                scope.launch { refresh() }
            },
            isRefreshing = refreshing,
            state = pullToRefreshState,
            indicator = {
                LoadingIndicator(
                    modifier = Modifier.align(Alignment.TopCenter),
                    isRefreshing = refreshing,
                    state = pullToRefreshState
                )
            }
        ) {
            content()
        } else content()
    }
}