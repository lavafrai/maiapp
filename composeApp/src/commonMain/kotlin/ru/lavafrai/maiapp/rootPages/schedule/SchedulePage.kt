@file:OptIn(ExperimentalSharedTransitionApi::class)

package ru.lavafrai.maiapp.rootPages.schedule

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import compose.icons.FeatherIcons
import compose.icons.feathericons.ArrowLeft
import maiapp.composeapp.generated.resources.Res
import maiapp.composeapp.generated.resources.schedule
import org.jetbrains.compose.resources.stringResource
import ru.lavafrai.maiapp.models.schedule.ScheduleId
import ru.lavafrai.maiapp.rootPages.main.MainPageTitle

@Composable
fun SchedulePage(
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onNavigateBack: () -> Unit,
    scheduleId: ScheduleId,
) {
    MainPageTitle(
        titleText = { Text(stringResource(Res.string.schedule)) },
        subtitleText = { Text(scheduleId.scheduleId) },
        leftButton = {
            IconButton(onClick = onNavigateBack) {
                Icon(FeatherIcons.ArrowLeft, "Back")
            }
        }
    )
}