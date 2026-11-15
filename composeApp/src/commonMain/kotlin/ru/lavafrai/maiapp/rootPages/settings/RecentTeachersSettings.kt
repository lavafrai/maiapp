package ru.lavafrai.maiapp.rootPages.settings

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.sp
import maiapp.composeapp.generated.resources.Res
import maiapp.composeapp.generated.resources.cancel
import maiapp.composeapp.generated.resources.got_it
import maiapp.composeapp.generated.resources.recent_teachers
import maiapp.composeapp.generated.resources.recent_teachers_limit
import maiapp.composeapp.generated.resources.recent_teachers_share_off
import maiapp.composeapp.generated.resources.recent_teachers_share_on
import maiapp.composeapp.generated.resources.recent_teachers_share_title
import maiapp.composeapp.generated.resources.recent_teachers_shared
import org.jetbrains.compose.resources.stringResource
import ru.lavafrai.maiapp.data.settings.ApplicationSettings
import ru.lavafrai.maiapp.data.settings.rememberSettings

@Composable
fun RecentTeachersSettings() = SettingsSection(title = stringResource(Res.string.recent_teachers)) {
    val settings by rememberSettings()
    // Какой вариант включить, если пользователь ещё не подтвердил смену списка
    var pendingShare by remember { mutableStateOf<Boolean?>(null) }
    val limitOptions = remember(settings.recentTeachersLimit) {
        (listOf(5, 10, 15, 20, 30) + settings.recentTeachersLimit).distinct().sorted()
    }
    val hasRecent = settings.recentScheduleTeachers.isNotEmpty() || settings.recentReviewTeachers.isNotEmpty()

    SettingsToggle(
        description = { Text(stringResource(Res.string.recent_teachers_shared)) },
        toggled = settings.shareRecentTeachers,
        onToggle = { shared ->
            // Если запоминать ещё нечего, спрашивать не о чем
            if (hasRecent) pendingShare = shared
            else ApplicationSettings.setShareRecentTeachers(shared)
        },
    )

    SettingsDropdownItem(
        title = stringResource(Res.string.recent_teachers_limit),
        items = limitOptions,
        selected = settings.recentTeachersLimit,
        onItemSelected = { ApplicationSettings.setRecentTeachersLimit(it) },
        itemContent = { Text(it.toString(), fontSize = 18.sp) },
    )

    pendingShare?.let { shared ->
        AlertDialog(
            onDismissRequest = { pendingShare = null },
            title = { Text(stringResource(Res.string.recent_teachers_share_title)) },
            text = {
                Text(
                    stringResource(
                        if (shared) Res.string.recent_teachers_share_on
                        else Res.string.recent_teachers_share_off
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        ApplicationSettings.setShareRecentTeachers(shared)
                        pendingShare = null
                    }
                ) { Text(stringResource(Res.string.got_it)) }
            },
            dismissButton = {
                TextButton(onClick = { pendingShare = null }) { Text(stringResource(Res.string.cancel)) }
            },
        )
    }
}
