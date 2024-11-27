@file:OptIn(ExperimentalSharedTransitionApi::class)

package ru.lavafrai.maiapp.navigation.rootPages.main

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import compose.icons.FeatherIcons
import compose.icons.feathericons.CloudOff
import compose.icons.feathericons.DownloadCloud
import maiapp.composeapp.generated.resources.Res
import maiapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import ru.lavafrai.maiapp.data.LoadableStatus
import ru.lavafrai.maiapp.data.settings.rememberSettings
import ru.lavafrai.maiapp.fragments.animations.pulsatingTransparency
import ru.lavafrai.maiapp.fragments.schedule.ScheduleView
import ru.lavafrai.maiapp.fragments.settings.ThemeSelectButton
import ru.lavafrai.maiapp.viewmodels.main.MainPageViewModel

@Composable
fun MainPage(
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onClearSettings: () -> Unit,
) {
    val settings by rememberSettings()
    val viewModel: MainPageViewModel = viewModel(
        factory = MainPageViewModel.Factory(
            onClearSettings = onClearSettings,
        )
    )
    val viewState by viewModel.state.collectAsState()
    var weekSelectorExpanded by remember { mutableStateOf(false) }

    Column {
        /*MainPageTitle(
            modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
            titleText = {
                Text(stringResource(Res.string.schedule))
            },
            subtitleText = {
                Text(settings.selectedSchedule!!)
            },
            rightButton = {
                Button(onClick = { weekSelectorExpanded = !weekSelectorExpanded }) {
                    Text(stringResource(Res.string.select_week))
                }
            }
        )*/

        MainPageNavigation(
            header = { page ->
                when (page) {
                    MainNavigationPageId.HOME -> MainPageTitle(
                        titleText = { Text(stringResource(Res.string.schedule)) },
                        subtitleText = { Text(settings.selectedSchedule!!) },
                        rightButton = {
                            TextButton(onClick = { weekSelectorExpanded = !weekSelectorExpanded }) {
                                Text(stringResource(Res.string.select_week))
                            }
                        },
                        subtitleIcon = { size ->
                            if (viewState.schedule.status == LoadableStatus.Offline) Icon(
                                imageVector = FeatherIcons.CloudOff,
                                contentDescription = "offline",
                                modifier = Modifier.size(size),
                            )
                            if (viewState.schedule.status == LoadableStatus.Updating) Icon(
                                imageVector = FeatherIcons.DownloadCloud,
                                contentDescription = "updating",
                                modifier = Modifier.size(size).pulsatingTransparency(),
                            )
                        },
                    )
                    MainNavigationPageId.SETTINGS -> MainPageTitle(
                        titleText = { Text(stringResource(Res.string.settings)) },
                        subtitleText = { Text(settings.selectedSchedule!!) },
                    )
                }
            },
        ) { page ->
            when (page) {
                MainNavigationPageId.HOME -> {
                    when (viewState.schedule.status) {
                        LoadableStatus.Loading -> CircularProgressIndicator()
                        LoadableStatus.Actual, LoadableStatus.Updating, LoadableStatus.Offline -> ScheduleView(
                            schedule = viewState.schedule.data!!,
                            dateRange = viewState.selectedWeek,
                            modifier = Modifier.fillMaxSize(),
                        )
                        LoadableStatus.Error -> Text("Error")
                    }
                }

                MainNavigationPageId.SETTINGS -> Column(Modifier.fillMaxSize()) {
                    Text("Settings")
                    ThemeSelectButton {
                        viewModel.setTheme(it)
                    }
                }
            }
        }
    }

    WeekSelector(
        onWeekSelected = { dateRange ->
            viewModel.setWeek(dateRange)
        },
        selectedWeek = viewState.selectedWeek,
        expanded = weekSelectorExpanded,
        onDismissRequest = { weekSelectorExpanded = false },
    )
}
