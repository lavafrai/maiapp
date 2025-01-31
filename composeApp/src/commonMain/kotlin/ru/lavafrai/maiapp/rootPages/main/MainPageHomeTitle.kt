@file:OptIn(ExperimentalMaterial3Api::class)

package ru.lavafrai.maiapp.rootPages.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import compose.icons.FeatherIcons
import compose.icons.feathericons.CloudOff
import compose.icons.feathericons.DownloadCloud
import kotlinx.coroutines.launch
import ru.lavafrai.maiapp.data.Loadable
import ru.lavafrai.maiapp.data.LoadableStatus
import ru.lavafrai.maiapp.data.settings.ApplicationSettings
import ru.lavafrai.maiapp.data.settings.rememberSettings
import ru.lavafrai.maiapp.fragments.animations.pulsatingTransparency
import ru.lavafrai.maiapp.models.schedule.Schedule

@Composable
fun MainPageHomeTitle(
    title: String,
    schedule: Loadable<Schedule>,
    buttonText: String,
    onButtonClick: () -> Unit,
) {
    val settings by rememberSettings()
    val otherSchedules =
        remember(settings) { settings.savedSchedules.filter { it.scheduleId != settings.selectedSchedule?.scheduleId } }
    var expanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            if (otherSchedules.isNotEmpty()) expanded = !expanded
        },
    ) {
        MainPageTitle(
            titleText = { Text(title) },
            subtitleText = {
                Text(
                    settings.selectedSchedule!!.toString(),
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = {
                        expanded = false
                    },
                ) {
                    //Column(modifier = Modifier.width(IntrinsicSize.Min)) {
                    otherSchedules.forEach { schedule ->
                        DropdownMenuItem(
                            text = { Text(schedule.toString()) },
                            onClick = {
                                expanded = false
                                scope.launch {
                                    ApplicationSettings.setSelectedGroup(schedule)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    //}

                }
            },
            rightButton = {
                TextButton(onClick = onButtonClick, enabled = schedule.hasData()) {
                    Text(buttonText)
                }
            },
            subtitleIcon = { size ->
                if (schedule.status == LoadableStatus.Offline) Icon(
                    imageVector = FeatherIcons.CloudOff,
                    contentDescription = "offline",
                    modifier = Modifier.size(size).alpha(0.7f),
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