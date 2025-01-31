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
import co.touchlab.kermit.Logger
import maiapp.composeapp.generated.resources.Res
import maiapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import ru.lavafrai.maiapp.data.LoadableStatus
import ru.lavafrai.maiapp.data.settings.VersionInfo
import ru.lavafrai.maiapp.data.settings.rememberSettings
import ru.lavafrai.maiapp.fragments.ErrorView
import ru.lavafrai.maiapp.fragments.LoadableView
import ru.lavafrai.maiapp.fragments.UpdateInfoDialog
import ru.lavafrai.maiapp.fragments.schedule.ScheduleView
import ru.lavafrai.maiapp.rootPages.maidata.MaiDataView
import ru.lavafrai.maiapp.rootPages.settings.SettingsPage
import ru.lavafrai.maiapp.utils.anySelector
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

    LaunchedEffect(settings.selectedSchedule) {
        viewModel.reloadSchedule(settings.selectedSchedule)
    }
    val viewState by viewModel.state.collectAsState()

    Column {
        MainPageNavigation(
            page = viewState.page,
            setPage = { page -> viewModel.setPage(page) },
            header = { page ->
                when (page) {
                    MainNavigationPageId.INFORMATION -> MainPageTitle(
                        titleText = { Text(stringResource(Res.string.information)) },
                        subtitleText = { Text(settings.selectedSchedule!!.toString()) },
                    )

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
                        onButtonClick = { weekSelectorExpanded = true },
                    )

                    MainNavigationPageId.ACCOUNT -> MainPageHomeTitle(
                        title = stringResource(Res.string.account),
                        schedule = viewState.schedule,
                    )

                    MainNavigationPageId.SETTINGS -> MainPageHomeTitle(
                        title = stringResource(Res.string.settings),
                        schedule = viewState.schedule,
                    )
                }
            },
        ) { page ->
            when (page) {
                MainNavigationPageId.INFORMATION -> {
                    LoadableView(state = viewState.maidata, retry = viewModel::startLoading) {
                        MaiDataView(
                            data = viewState.maidata.data!!,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }

                MainNavigationPageId.WORKS -> {
                    LoadableView(state = viewState.schedule, retry = viewModel::startLoading) {
                        ScheduleView(
                            schedule = viewState.schedule.data!!,
                            exlerTeachers = viewState.exlerTeachers.data,
                            dateRange = null,
                            modifier = Modifier.fillMaxSize(),
                            selector = remember (viewState.workLessonSelectors) { viewState.workLessonSelectors.anySelector() },
                        )
                    }
                }

                MainNavigationPageId.HOME -> {
                    LoadableView(state = viewState.schedule, retry = viewModel::startLoading) {
                        ScheduleView(
                            schedule = viewState.schedule.data!!,
                            exlerTeachers = viewState.exlerTeachers.data,
                            dateRange = viewState.selectedWeek,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }

                MainNavigationPageId.ACCOUNT -> Column(Modifier.fillMaxSize()) {
                    // AccountPage()
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

        currentLessonSelectors = viewState.workLessonSelectors,
        onSelectorsChanged = { lessonTypes ->
            viewModel.setWorksLessonSelector(lessonTypes)
        },
    )
}
