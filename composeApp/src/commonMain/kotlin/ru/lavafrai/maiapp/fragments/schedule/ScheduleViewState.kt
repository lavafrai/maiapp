package ru.lavafrai.maiapp.fragments.schedule

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.datetime.LocalDate
import ru.lavafrai.maiapp.models.schedule.Schedule
import ru.lavafrai.maiapp.models.schedule.ScheduleDay
import ru.lavafrai.maiapp.models.time.DateRange
import ru.lavafrai.maiapp.models.time.now
import ru.lavafrai.maiapp.utils.LessonSelector

class ScheduleViewState(
    firstVisibleItemIndex: Int = 0,
    firstVisibleItemScrollOffset: Int = 0,
    private var lastLessonsHash: Int = 0,
) {
    val lazyScrollState = LazyListState(
        firstVisibleItemIndex = firstVisibleItemIndex,
        firstVisibleItemScrollOffset = firstVisibleItemScrollOffset,
    )

    suspend fun updateScrollIfRequired(
        schedule: Schedule,
        dateRange: DateRange?,
        selector: LessonSelector,
        filteredLessons: List<ScheduleDay>,
    ) {
        val newLessonsHash = filteredLessons.fold(0) { acc, day ->
            acc * 31 + day.hashCode()
        }
        if (newLessonsHash == 0) return
        if (newLessonsHash != lastLessonsHash) {
            val pairsFinishedToday = filteredLessons.find { it.date == LocalDate.now() }?.isFinished() ?: false
            var index = filteredLessons.indexOfFirst { it.date >= LocalDate.now() } + if (pairsFinishedToday) 1 else 0
            if (index == -1) index = filteredLessons.size - 1

            lazyScrollState.scrollToItem((index * 2).coerceIn(0, Int.MAX_VALUE))
            lastLessonsHash = newLessonsHash
        }
    }

    companion object {
        val Saver: Saver<ScheduleViewState, *> = listSaver(
            save = { listOf(
                it.lazyScrollState.firstVisibleItemIndex,
                it.lazyScrollState.firstVisibleItemScrollOffset,
                it.lastLessonsHash,
            ) },
            restore = { ScheduleViewState(
                firstVisibleItemIndex = it[0] as Int,
                firstVisibleItemScrollOffset = it[1] as Int,
                lastLessonsHash = it[2] as Int,
            ) },
        )
    }
}

@Composable
fun rememberScheduleViewState() = rememberSaveable(saver = ScheduleViewState.Saver) { ScheduleViewState() }