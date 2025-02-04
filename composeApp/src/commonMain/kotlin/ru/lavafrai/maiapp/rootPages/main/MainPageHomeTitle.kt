@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package ru.lavafrai.maiapp.rootPages.main

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import compose.icons.FeatherIcons
import compose.icons.feathericons.CloudOff
import compose.icons.feathericons.DownloadCloud
import kotlinx.coroutines.launch
import ru.lavafrai.maiapp.LocalApplicationContext
import ru.lavafrai.maiapp.data.Loadable
import ru.lavafrai.maiapp.data.LoadableStatus
import ru.lavafrai.maiapp.data.settings.ApplicationSettings
import ru.lavafrai.maiapp.data.settings.rememberSettings
import ru.lavafrai.maiapp.fragments.animations.pulsatingTransparency
import ru.lavafrai.maiapp.models.schedule.Schedule
import ru.lavafrai.maiapp.utils.contextual

@Composable
fun MainPageHomeTitle(
    title: String,
    schedule: Loadable<Schedule>,
    buttonText: String? = null,
    onButtonClick: () -> Unit = { },
    onRequestRefresh: () -> Unit = { },
) {
    val haptic = LocalHapticFeedback.current
    val appContext = LocalApplicationContext.current
    val settings by rememberSettings()
    val otherSchedules =
        remember(settings) { settings.savedSchedules.filter { it.scheduleId != settings.selectedSchedule?.scheduleId } }
    var expanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {

        },
    ) {
        MainPageTitle(
            titleText = { Text(title) },
            subtitleText = {
                Text(
                    settings.selectedSchedule!!.toString(),
                    modifier = Modifier
                        .padding(end = 2.dp)
                        .clip(MaterialTheme.shapes.small)
                        .contextual(otherSchedules.isNotEmpty() && schedule.isNotLoading()) {
                            clickable { expanded = true }
                        },
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
                if (buttonText != null) TextButton(onClick = onButtonClick, enabled = schedule.hasData()) {
                    Text(buttonText)
                }
            },
            subtitleIcon = { size ->
                if (schedule.status == LoadableStatus.Offline) Icon(
                    imageVector = FeatherIcons.CloudOff,
                    contentDescription = "offline",
                    modifier = Modifier
                        .size(size)
                        .alpha(0.7f)
                        .clickable(onClick = onRequestRefresh),
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