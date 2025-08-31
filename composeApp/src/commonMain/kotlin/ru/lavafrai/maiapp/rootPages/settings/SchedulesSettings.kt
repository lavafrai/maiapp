@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package ru.lavafrai.maiapp.rootPages.settings

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.shimmer
import compose.icons.FeatherIcons
import compose.icons.feathericons.ChevronDown
import compose.icons.feathericons.Trash2
import kotlinx.coroutines.launch
import maiapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import ru.lavafrai.maiapp.LocalApplicationContext
import ru.lavafrai.maiapp.data.Loadable
import ru.lavafrai.maiapp.data.repositories.ScheduleRepository
import ru.lavafrai.maiapp.data.settings.ApplicationSettings
import ru.lavafrai.maiapp.data.settings.rememberSettings
import ru.lavafrai.maiapp.models.schedule.Schedule
import ru.lavafrai.maiapp.navigation.pages.LoginPage
import ru.lavafrai.maiapp.utils.conditional
import ru.lavafrai.maiapp.utils.formatBinarySize
import ru.lavafrai.maiapp.viewmodels.login.LoginTarget
import ru.lavafrai.maiapp.viewmodels.login.LoginType

@Composable
fun SchedulesSettings(
    schedule: Loadable<Schedule>,
) = SettingsSection(title = stringResource(Res.string.schedule)) {
    val scheduleRepository = ScheduleRepository()
    val appContext = LocalApplicationContext.current
    val scope = rememberCoroutineScope()
    val settings by rememberSettings()
    val otherSchedules = remember(settings) {
        settings.savedSchedules.filter { it.scheduleId != settings.selectedSchedule?.scheduleId }
    }
    var expanded by remember { mutableStateOf(false) }
    val openButtonRotationDegrees by animateFloatAsState(if (expanded) -180f else 0f)

    LaunchedEffect(otherSchedules) {
        if (otherSchedules.isEmpty()) expanded = false
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            // expanded = !expanded
        },
    ) {
        OutlinedTextField(
            value = settings.selectedSchedule?.scheduleId ?: stringResource(Res.string.unknown),
            onValueChange = {},
            enabled = false,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(type = MenuAnchorType.PrimaryNotEditable),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            trailingIcon = {
                if (otherSchedules.isNotEmpty()) IconButton(onClick = { expanded = !expanded }, enabled = schedule.isNotLoading()) {
                    Icon(
                        imageVector = FeatherIcons.ChevronDown,
                        contentDescription = stringResource(Res.string.schedule),
                        modifier = Modifier.rotate(openButtonRotationDegrees),
                    )
                }
            }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            },
        ) {
            otherSchedules.forEach { schedule ->
                var calculatedSize by remember { mutableStateOf(null as Long?) }
                LaunchedEffect(schedule) {
                    val size = scheduleRepository.getScheduleSizeInCache(schedule)
                    calculatedSize = size
                }

                DropdownMenuItem(
                    text = {
                        Text(text = schedule.scheduleId)
                    },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (calculatedSize != null) calculatedSize!!.formatBinarySize() else "100hn",
                                modifier = Modifier
                                    .alpha(0.7f)
                                    .conditional(calculatedSize == null) { shimmer() }
                            )
                            IconButton(onClick = { scope.launch { ApplicationSettings.removeSavedGroup(schedule) } }) {
                                Icon(imageVector = FeatherIcons.Trash2, contentDescription = null)
                            }
                        }
                    },
                    onClick = {
                        scope.launch {
                            ApplicationSettings.setSelectedSchedule(schedule)
                        }
                        expanded = false
                    }
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))
    Text(text = stringResource(Res.string.add_schedule_description))

    Spacer(modifier = Modifier.height(8.dp))
    Button(
        onClick = { appContext.navController.navigate(LoginPage(type = LoginType.STUDENT, target = LoginTarget.ADD_SCHEDULE, navigateImmediately = true)) },
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(text = stringResource(Res.string.add_schedule))
    }

    Spacer(Modifier.height(8.dp))
    HorizontalDivider()
    Spacer(Modifier.height(8.dp))
    SettingsToggle(
        description = { Text(stringResource(Res.string.hide_military_training)) },
        toggled = settings.hideMilitaryTraining,
        onToggle = { ApplicationSettings.setHideMilitaryTraining(it) },
        enabled = schedule.isNotLoading() && otherSchedules.isNotEmpty(),
    )
}