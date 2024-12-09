package ru.lavafrai.maiapp.navigation.rootPages.main

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import compose.icons.FeatherIcons
import compose.icons.feathericons.CloudOff
import compose.icons.feathericons.DownloadCloud
import ru.lavafrai.maiapp.data.Loadable
import ru.lavafrai.maiapp.data.LoadableStatus
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
    MainPageTitle(
        titleText = { Text(title) },
        subtitleText = { Text(settings.selectedSchedule!!) },
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
    )
}