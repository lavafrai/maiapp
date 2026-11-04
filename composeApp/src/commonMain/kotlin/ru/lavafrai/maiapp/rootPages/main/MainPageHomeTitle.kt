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
import androidx.compose.ui.unit.dp
import compose.icons.FeatherIcons
import compose.icons.feathericons.Check
import compose.icons.feathericons.CloudOff
import compose.icons.feathericons.DownloadCloud
import compose.icons.feathericons.Edit3
import kotlinx.coroutines.launch
import maiapp.composeapp.generated.resources.Res
import maiapp.composeapp.generated.resources.schedule_current
import maiapp.composeapp.generated.resources.schedule_label
import org.jetbrains.compose.resources.stringResource
import ru.lavafrai.maiapp.LocalApplicationContext
import ru.lavafrai.maiapp.data.Loadable
import ru.lavafrai.maiapp.data.LoadableStatus
import ru.lavafrai.maiapp.data.repositories.readableName
import ru.lavafrai.maiapp.data.settings.ApplicationSettings
import ru.lavafrai.maiapp.data.settings.rememberSettings
import ru.lavafrai.maiapp.fragments.animations.pulsatingTransparency
import ru.lavafrai.maiapp.fragments.schedule.ScheduleName
import ru.lavafrai.maiapp.models.events.Event
import ru.lavafrai.maiapp.models.schedule.Schedule
import ru.lavafrai.maiapp.rootPages.settings.ScheduleLabelDialog
import ru.lavafrai.maiapp.utils.contextual

@Composable
fun MainPageHomeTitle(
    title: String,
    schedule: Loadable<Schedule>,
    events: Loadable<List<Event>>,
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
    // В списке показываем и текущее расписание: его подпись можно править, не переключаясь на другое
    val allSchedules = remember(settings, otherSchedules) {
        listOfNotNull(settings.selectedSchedule) + otherSchedules
    }
    var expanded by remember { mutableStateOf(false) }
    // id расписания, для которого открыт диалог подписи и цвета
    var labelDialogScheduleId by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val localClipboardManager = LocalClipboardManager.current

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {

        },
    ) {
        MainPageTitle(
            titleText = { Text(title) },
            subtitleText = {
                ScheduleName(
                    name = settings.selectedSchedule!!.readableName(),
                    label = settings.selectedSchedule?.let { settings.scheduleLabels[it.scheduleId] },
                    modifier = Modifier
                        .padding(end = 2.dp)
                        .clip(MaterialTheme.shapes.small)
                        .contextual(otherSchedules.isNotEmpty() && schedule.isNotLoading()) {
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
                    allSchedules.forEach { schedule ->
                        val isCurrent = schedule.scheduleId == settings.selectedSchedule?.scheduleId
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
                                        if (!isCurrent) scope.launch {
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
                                // Отступы по вертикали минимальные: иконка-кнопка и так даёт 48dp высоты
                                .padding(start = 12.dp, top = 2.dp, bottom = 2.dp, end = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            ScheduleName(
                                name = schedule.scheduleId,
                                label = settings.scheduleLabels[schedule.scheduleId],
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.weight(1f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                            if (isCurrent) Icon(
                                imageVector = FeatherIcons.Check,
                                contentDescription = stringResource(Res.string.schedule_current),
                            )
                            IconButton(onClick = {
                                expanded = false
                                labelDialogScheduleId = schedule.scheduleId
                            }) {
                                Icon(
                                    imageVector = FeatherIcons.Edit3,
                                    contentDescription = stringResource(Res.string.schedule_label),
                                )
                            }
                        }
                    }
                    //}

                }
            },
            rightButton = {
                Row(verticalAlignment = Alignment.CenterVertically) {
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
                    if (buttonText != null) TextButton(onClick = onButtonClick, enabled = schedule.hasData() && events.hasData(), shapes = ButtonDefaults.shapes()) {
                        Text(buttonText)
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

    labelDialogScheduleId?.let { scheduleId ->
        ScheduleLabelDialog(
            scheduleId = scheduleId,
            onDismiss = { labelDialogScheduleId = null },
        )
    }
}