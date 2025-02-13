@file:OptIn(ExperimentalSharedTransitionApi::class)

package ru.lavafrai.maiapp.rootPages.schedule

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import compose.icons.FeatherIcons
import compose.icons.feathericons.ArrowLeft
import maiapp.composeapp.generated.resources.Res
import maiapp.composeapp.generated.resources.schedule
import org.jetbrains.compose.resources.stringResource
import ru.lavafrai.maiapp.fragments.LoadableView
import ru.lavafrai.maiapp.fragments.schedule.ScheduleView
import ru.lavafrai.maiapp.models.schedule.ScheduleId
import ru.lavafrai.maiapp.rootPages.main.MainPageTitle
import ru.lavafrai.maiapp.viewmodels.dedicatedSchedule.DedicatedScheduleViewModel

@Composable
fun DedicatedSchedulePage(
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onNavigateBack: () -> Unit,
    scheduleId: ScheduleId,
    title: String? = null,
) {
    val viewModel: DedicatedScheduleViewModel = viewModel(factory = DedicatedScheduleViewModel.Factory(scheduleId))
    val viewState by viewModel.state.collectAsState()
    Column {
        MainPageTitle(
            titleText = { Text(stringResource(Res.string.schedule)) },
            subtitleText = { Text(title ?: scheduleId.scheduleId) },
            leftButton = {
                IconButton(onClick = onNavigateBack) {
                    Icon(FeatherIcons.ArrowLeft, "Back")
                }
            }
        )

        LoadableView(
            viewState.schedule,
            retry = viewModel::startLoading
        ) {
            ScheduleView(
                schedule = it,
                exlerTeachers = viewState.exlerTeachers.data,
            )
        }
    }
}