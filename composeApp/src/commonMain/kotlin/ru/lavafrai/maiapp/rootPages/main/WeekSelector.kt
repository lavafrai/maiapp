@file:OptIn(ExperimentalMaterial3Api::class)

package ru.lavafrai.maiapp.rootPages.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import compose.icons.FeatherIcons
import compose.icons.feathericons.ArrowLeft
import compose.icons.feathericons.ArrowRight
import compose.icons.feathericons.Home
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import maiapp.composeapp.generated.resources.Res
import maiapp.composeapp.generated.resources.current_week
import maiapp.composeapp.generated.resources.last_week
import maiapp.composeapp.generated.resources.next_week
import org.jetbrains.compose.resources.stringResource
import ru.lavafrai.maiapp.fragments.schedule.PairNumber
import ru.lavafrai.maiapp.models.events.Event
import ru.lavafrai.maiapp.models.schedule.Schedule
import ru.lavafrai.maiapp.models.time.DateRange
import ru.lavafrai.maiapp.models.time.now
import ru.lavafrai.maiapp.models.time.week

@Composable
fun WeekSelector(
    onWeekSelected: (DateRange) -> Unit,
    schedule: Schedule,
    events: List<Event>,
    selectedWeek: DateRange,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
) {
    var visible by remember { mutableStateOf(expanded) }
    val scope = rememberCoroutineScope()
    val weeks = remember(schedule) { schedule.weeks(events) }
    val modalBottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false,
    )
    val close = {
        scope.launch {
            modalBottomSheetState.hide()
        }.invokeOnCompletion {
            visible = false
            onDismissRequest()
        }
    }

    LaunchedEffect(expanded) {
        if (expanded) {
            visible = true
        } else {
            close()
        }
    }


    if (visible) ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = modalBottomSheetState,
        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
    ) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            // Special item for current week

            ListItem(
                headlineContent = { Text(stringResource(Res.string.last_week)) },
                leadingContent = { Icon(FeatherIcons.ArrowLeft, "Last week", modifier = Modifier.size(24.dp)) },
                modifier = Modifier
                    .clickable { onWeekSelected(selectedWeek.plusDays(-7)) ; close() }
            )

            ListItem(
                headlineContent = { Text(stringResource(Res.string.current_week)) },
                leadingContent = { Icon(FeatherIcons.Home, "Current week", modifier = Modifier.size(24.dp)) },
                modifier = Modifier
                    .clickable { onWeekSelected(LocalDate.now().week()) ; close() }
            )

            ListItem(
                headlineContent = { Text(stringResource(Res.string.next_week)) },
                leadingContent = { Icon(FeatherIcons.ArrowRight, "Next week", modifier = Modifier.size(24.dp)) },
                modifier = Modifier
                    .clickable { onWeekSelected(selectedWeek.plusDays(7)) ; close() }
            )

            Spacer(
                modifier = Modifier
                    .height(16.dp)
                    .fillMaxWidth()
            )
            // Sheet content
            weeks.forEachIndexed { index, week ->
                // WeekSelectorItem
                val weekIsCurrent = week == LocalDate.now().week()
                val weekIsSelected = week == selectedWeek

                ListItem(
                    headlineContent = { Text(week.toString()) },
                    leadingContent = {
                        PairNumber(
                            text = "${index + 1}",
                            background = if (weekIsCurrent) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                        )
                    },
                    modifier = Modifier
                        .clickable { onWeekSelected(week) ; close() },
                    colors = if (weekIsSelected) ListItemDefaults.colors(containerColor = Color.Transparent) else ListItemDefaults.colors()
                )

                /*Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .conditional(weekIsSelected) { background(MaterialTheme.colorScheme.surfaceVariant) }
                        .clickable { onWeekSelected(week) ; close() }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PairNumber(
                        text = "${index + 1}",
                        background = if (weekIsCurrent) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = week.toString(),
                        modifier = Modifier.padding(start = 16.dp),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }*/
            }
        }
    }
}
