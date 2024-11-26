@file:OptIn(ExperimentalFoundationApi::class, ExperimentalHazeMaterialsApi::class)

package ru.lavafrai.maiapp.fragments.schedule

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.materials.CupertinoMaterials
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import ru.lavafrai.maiapp.models.schedule.Schedule
import ru.lavafrai.maiapp.models.time.DateRange

@Composable
fun ScheduleView(
    schedule: Schedule,
    dateRange: DateRange? = null,
    modifier: Modifier = Modifier,
) {
    val filteredDays = remember(dateRange) { schedule.days.filter { if (dateRange == null) true else it.date!! in dateRange } }

    LazyColumn(
        modifier = modifier,
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