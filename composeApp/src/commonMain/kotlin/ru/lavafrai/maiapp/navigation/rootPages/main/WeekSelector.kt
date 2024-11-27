@file:OptIn(ExperimentalMaterial3Api::class)

package ru.lavafrai.maiapp.navigation.rootPages.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import ru.lavafrai.maiapp.models.schedule.Schedule
import ru.lavafrai.maiapp.models.time.DateRange
import ru.lavafrai.maiapp.models.time.now
import ru.lavafrai.maiapp.models.time.week
import ru.lavafrai.maiapp.utils.conditional

@Composable
fun WeekSelector(
    onWeekSelected: (DateRange) -> Unit,
    schedule: Schedule,
    selectedWeek: DateRange,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
) {
    var visible by remember { mutableStateOf(expanded) }
    val scope = rememberCoroutineScope()
    val weeks = remember(schedule) { schedule.weeks }
    val modalBottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false,
    )
    val close = {
        scope.launch {
            modalBottomSheetState.hide()
        }.invokeOnCompletion {
            visible = false
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
        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            // Special item for current week
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onWeekSelected(selectedWeek.plusDays(-7)) ; close() }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(FeatherIcons.ArrowLeft, "Last week", modifier = Modifier.size(24.dp))
                Text(
                    text = stringResource(Res.string.last_week),
                    modifier = Modifier.padding(start = 16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onWeekSelected(LocalDate.now().week()) ; close() }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(FeatherIcons.Home, "Current week", modifier = Modifier.size(24.dp))
                Text(
                    text = stringResource(Res.string.current_week),
                    modifier = Modifier.padding(start = 16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onWeekSelected(selectedWeek.plusDays(7)) ; close() }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(FeatherIcons.ArrowRight, "Next week", modifier = Modifier.size(24.dp))
                Text(
                    text = stringResource(Res.string.next_week),
                    modifier = Modifier.padding(start = 16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            // Sheet content
            weeks.forEachIndexed { index, week ->
                // WeekSelectorItem
                val weekIsCurrent = week == LocalDate.now().week()
                val weekIsSelected = week == selectedWeek

                Row(
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
                }
            }
        }
    }
}
