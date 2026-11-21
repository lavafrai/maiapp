@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package ru.lavafrai.maiapp.rootPages.main

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import compose.icons.FeatherIcons
import compose.icons.feathericons.ArrowLeft
import compose.icons.feathericons.ArrowRight
import compose.icons.feathericons.CloudOff
import compose.icons.feathericons.DownloadCloud
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import maiapp.composeapp.generated.resources.Res
import maiapp.composeapp.generated.resources.last_week
import maiapp.composeapp.generated.resources.next_week
import org.jetbrains.compose.resources.stringResource
import ru.lavafrai.maiapp.LocalApplicationContext
import ru.lavafrai.maiapp.data.Loadable
import ru.lavafrai.maiapp.data.LoadableStatus
import ru.lavafrai.maiapp.data.repositories.readableName
import ru.lavafrai.maiapp.data.settings.ApplicationSettings
import ru.lavafrai.maiapp.data.settings.rememberSettings
import ru.lavafrai.maiapp.fragments.animations.pulsatingTransparency
import ru.lavafrai.maiapp.fragments.schedule.PairNumber
import ru.lavafrai.maiapp.models.events.Event
import ru.lavafrai.maiapp.models.schedule.Schedule
import ru.lavafrai.maiapp.models.time.DateRange
import ru.lavafrai.maiapp.models.time.now
import ru.lavafrai.maiapp.models.time.week
import ru.lavafrai.maiapp.utils.contextual

@Composable
fun MainPageHomeTitle(
    title: String,
    schedule: Loadable<Schedule>,
    events: Loadable<List<Event>>,
    selectedWeek: DateRange = DateRange.currentWeek(),
    onWeekSelected: (DateRange) -> Unit = { },
    showWeekSwitcher: Boolean = false,
    buttonText: String? = null,
    onButtonClick: () -> Unit = { },
    additionalButtonContent: (@Composable () -> Unit)? = null,
    onAdditionalButtonClick: (() -> Unit)? = null,
    onAdditionalButtonLongClick: (() -> Unit)? = null,
    onRequestRefresh: () -> Unit = { },
) {
    val haptic = LocalHapticFeedback.current
    val appContext = LocalApplicationContext.current
    val settings by rememberSettings()
    val otherSchedules =
        remember(settings) { settings.savedSchedules.filter { it.scheduleId != settings.selectedSchedule?.scheduleId } }
    var expanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val localClipboardManager = LocalClipboardManager.current

    val weeks = remember(schedule, events, showWeekSwitcher) {
        val scheduleData = schedule.data
        val eventsData = events.data
        if (showWeekSwitcher && scheduleData != null && eventsData != null) scheduleData.weeks(eventsData)
        else emptyList()
    }
    val weekIndex = if (showWeekSwitcher) weeks.indexOf(selectedWeek) else -1
    val currentWeek = remember { LocalDate.now().week() }

    BoxWithConstraints {
        val narrowHeader = maxWidth < 420.dp

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {

            },
        ) {
            MainPageTitle(
            titleText = { Text(title) },
            subtitleText = {
                Text(
                    settings.selectedSchedule!!.readableName(),
                    modifier = Modifier
                        .padding(end = 2.dp)
                        .clip(MaterialTheme.shapes.small)
                        .contextual(otherSchedules.isNotEmpty()) {
                            clickable { expanded = true }
                        },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = {
                        expanded = false
                    },
                ) {
                    //Column(modifier = Modifier.width(IntrinsicSize.Min)) {
                    otherSchedules.forEach { schedule ->
                        /*DropdownMenuItem(
                            text = { Text(schedule.toString()) },
                            onClick = {},
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = {
                                        expanded = false
                                        scope.launch {
                                            ApplicationSettings.setSelectedGroup(schedule)
                                        }
                                    },
                                    onLongClick = {

                                    }
                                )
                        )*/

                        Row(
                            modifier = Modifier
                                .combinedClickable(
                                    onClick = {
                                        expanded = false
                                        scope.launch {
                                            ApplicationSettings.setSelectedGroup(schedule)
                                        }
                                    },
                                    onLongClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        appContext.openSchedule(schedule)
                                        expanded = false
                                    }
                                )
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Text(schedule.toString(), style = MaterialTheme.typography.labelLarge)
                        }
                    }
                    //}

                }
            },
            rightButton = {
                val headerPlus: @Composable () -> Unit = {
                    if (additionalButtonContent != null) Box(
                        modifier = Modifier
                            .size(40.dp) // размер зоны нажатия аналог IconButton
                            .clip(CircleShape)
                            .combinedClickable(
                                enabled = schedule.hasData() && events.hasData(),
                                role = Role.Button,
                                onClick = { onAdditionalButtonClick?.invoke() },
                                onLongClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onAdditionalButtonLongClick?.invoke()
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        additionalButtonContent()
                    }
                }

                val headerButton: @Composable () -> Unit = {
                    if (buttonText != null) TextButton(
                        onClick = onButtonClick,
                        enabled = schedule.hasData() && events.hasData(),
                        shapes = ButtonDefaults.shapes(),
                    ) {
                        Text(buttonText)
                    }
                }

                if (narrowHeader) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        headerPlus()
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            headerButton()
                            if (weekIndex >= 0) Row(verticalAlignment = Alignment.CenterVertically) {
                                WeekArrowButton(
                                    icon = FeatherIcons.ArrowLeft,
                                    contentDescription = stringResource(Res.string.last_week),
                                    enabled = weekIndex > 0,
                                    onClick = { onWeekSelected(weeks[weekIndex - 1]) },
                                )
                                PairNumber(
                                    text = "${weekIndex + 1}",
                                    background = if (selectedWeek == currentWeek) MaterialTheme.colorScheme.secondary
                                                 else MaterialTheme.colorScheme.primary,
                                    size = 32.dp,
                                )
                                WeekArrowButton(
                                    icon = FeatherIcons.ArrowRight,
                                    contentDescription = stringResource(Res.string.next_week),
                                    enabled = weekIndex < weeks.lastIndex,
                                    onClick = { onWeekSelected(weeks[weekIndex + 1]) },
                                )
                            }
                        }
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        headerPlus()
                        headerButton()

                        if (weekIndex >= 0) {
                            Spacer(Modifier.width(4.dp))
                            WeekArrowButton(
                                icon = FeatherIcons.ArrowLeft,
                                contentDescription = stringResource(Res.string.last_week),
                                enabled = weekIndex > 0,
                                onClick = { onWeekSelected(weeks[weekIndex - 1]) },
                            )
                            PairNumber(
                                text = "${weekIndex + 1}",
                                background = if (selectedWeek == currentWeek) MaterialTheme.colorScheme.secondary
                                             else MaterialTheme.colorScheme.primary,
                                size = 32.dp,
                            )
                            WeekArrowButton(
                                icon = FeatherIcons.ArrowRight,
                                contentDescription = stringResource(Res.string.next_week),
                                enabled = weekIndex < weeks.lastIndex,
                                onClick = { onWeekSelected(weeks[weekIndex + 1]) },
                            )
                        }
                    }
                }
            },
            subtitleIcon = { size ->
                if (schedule.status == LoadableStatus.Offline) Icon(
                    imageVector = FeatherIcons.CloudOff,
                    contentDescription = "offline",
                    modifier = Modifier
                        .size(size)
                        .alpha(0.7f)
                        .combinedClickable(
                            onClick = onRequestRefresh,
                            onLongClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                localClipboardManager.setText(buildAnnotatedString { append(schedule.error!!.stackTraceToString()) })
                            }
                        ),
                )
                if (schedule.status == LoadableStatus.Updating) Icon(
                    imageVector = FeatherIcons.DownloadCloud,
                    contentDescription = "updating",
                    modifier = Modifier.size(size).pulsatingTransparency(),
                )
            },
            modifier = Modifier.menuAnchor(type = MenuAnchorType.PrimaryNotEditable),
        )
        }
    }
}

@Composable
private fun WeekArrowButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
    size: Dp = 40.dp,
    iconSize: Dp = 20.dp,
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier
                .size(iconSize)
                .alpha(if (enabled) 1f else 0.3f),
        )
    }
}