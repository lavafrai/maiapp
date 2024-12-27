@file:OptIn(ExperimentalSharedTransitionApi::class)

package ru.lavafrai.maiapp.rootPages.main

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import maiapp.composeapp.generated.resources.Res
import maiapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import ru.lavafrai.maiapp.BuildConfig
import ru.lavafrai.maiapp.data.LoadableStatus
import ru.lavafrai.maiapp.data.settings.VersionInfo
import ru.lavafrai.maiapp.data.settings.rememberSettings
import ru.lavafrai.maiapp.fragments.ErrorView
import ru.lavafrai.maiapp.fragments.UpdateInfoDialog
import ru.lavafrai.maiapp.fragments.schedule.ScheduleView
import ru.lavafrai.maiapp.rootPages.settings.SettingsPage
import ru.lavafrai.maiapp.viewmodels.main.MainPageViewModel

@Composable
fun MainPage(
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onClearSettings: () -> Unit,
) {
    var weekSelectorExpanded by remember { mutableStateOf(false) }
    var workTypeSelectorExpanded by remember { mutableStateOf(false) }
    var updateInfoExpanded by remember { mutableStateOf(false) }
    val settings by rememberSettings()

    val viewModel: MainPageViewModel = viewModel(
        factory = MainPageViewModel.Factory(
            onClearSettings = onClearSettings,
            onShowUpdateInfo = {
                updateInfoExpanded = true
            },
        )
    )

    val viewState by viewModel.state.collectAsState()

    Column {
        MainPageNavigation(
            page = viewState.page,
            setPage = { page -> viewModel.setPage(page) },
            header = { page ->
                when (page) {
                    MainNavigationPageId.WORKS -> MainPageHomeTitle(
                        title = stringResource(Res.string.works),
                        schedule = viewState.schedule,
                        buttonText = stringResource(Res.string.work_type),
                        onButtonClick = { workTypeSelectorExpanded = !workTypeSelectorExpanded },
                    )

                    MainNavigationPageId.HOME -> MainPageHomeTitle(
                        title = stringResource(Res.string.schedule),
                        schedule = viewState.schedule,
                        buttonText = stringResource(Res.string.select_week),
                        onButtonClick = { weekSelectorExpanded = !weekSelectorExpanded },
                    )

                    MainNavigationPageId.SETTINGS -> MainPageTitle(
                        titleText = { Text(stringResource(Res.string.settings)) },
                        subtitleText = { Text(settings.selectedSchedule!!) },
                    )
                }
            },
        ) { page ->
            when (page) {
                MainNavigationPageId.WORKS -> {
                    when (viewState.schedule.status) {
                        LoadableStatus.Loading -> CircularProgressIndicator()
                        LoadableStatus.Actual, LoadableStatus.Updating, LoadableStatus.Offline -> ScheduleView(
                            schedule = viewState.schedule.data!!,
                            exlerTeachers = viewState.exlerTeachers.data,
                            dateRange = null,
                            modifier = Modifier.fillMaxSize(),
                            selector = remember (viewState.workTypeSelected) {{ _, lesson -> lesson.type in viewState.workTypeSelected }}
                        )
                        LoadableStatus.Error -> ErrorView(
                            error = viewState.schedule.error,
                            onRetry = { viewModel.startLoading() },
                        )
                    }
                }

                MainNavigationPageId.HOME -> {
                    when (viewState.schedule.status) {
                        LoadableStatus.Loading -> CircularProgressIndicator()
                        LoadableStatus.Actual, LoadableStatus.Updating, LoadableStatus.Offline -> ScheduleView(
                            schedule = viewState.schedule.data!!,
                            exlerTeachers = viewState.exlerTeachers.data,
                            dateRange = viewState.selectedWeek,
                            modifier = Modifier.fillMaxSize(),
                        )
                        LoadableStatus.Error -> ErrorView(
                            error = viewState.schedule.error,
                            onRetry = { viewModel.startLoading() },
                        )
                    }
                }

                MainNavigationPageId.SETTINGS -> Column(Modifier.fillMaxSize()) {
                    SettingsPage()
                }
            }
        }
    }

    if (updateInfoExpanded) {
        UpdateInfoDialog(
            lastVersion = VersionInfo.lastVersion,
            currentVersion = VersionInfo.currentVersion,
            onDismissRequest = { updateInfoExpanded = false ; VersionInfo.updateLastVersion() },
            onOkay = { updateInfoExpanded = false ; VersionInfo.updateLastVersion() },
        )
    }

    if (viewState.schedule.hasData()) WeekSelector(
        onWeekSelected = { dateRange ->
            viewModel.setWeek(dateRange)
        },
        selectedWeek = viewState.selectedWeek,
        expanded = weekSelectorExpanded,
        onDismissRequest = { weekSelectorExpanded = false },
        schedule = viewState.schedule.data!!,
    )

    if (viewState.schedule.hasData()) LessonTypeSelector(
        expanded = workTypeSelectorExpanded,
        onDismissRequest = { workTypeSelectorExpanded = false },
        selectedLessonTypes = viewState.workTypeSelected,
        onLessonTypeSelected = { lessonTypes ->
            viewModel.setSelectedWorkTypes(lessonTypes)
        },
    )
}
