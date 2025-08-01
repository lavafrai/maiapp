@file:OptIn(ExperimentalSharedTransitionApi::class)

package ru.lavafrai.maiapp.rootPages.main

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import maiapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import ru.lavafrai.maiapp.data.settings.VersionInfo
import ru.lavafrai.maiapp.data.settings.rememberSettings
import ru.lavafrai.maiapp.fragments.LoadableView
import ru.lavafrai.maiapp.fragments.UpdateInfoDialog
import ru.lavafrai.maiapp.fragments.account.AccountPage
import ru.lavafrai.maiapp.fragments.events.EventCreateDialog
import ru.lavafrai.maiapp.fragments.schedule.ScheduleView
import ru.lavafrai.maiapp.rootPages.maidata.MaiDataView
import ru.lavafrai.maiapp.rootPages.settings.SettingsPage
import ru.lavafrai.maiapp.utils.anySelector
import ru.lavafrai.maiapp.viewmodels.account.AccountViewModel
import ru.lavafrai.maiapp.viewmodels.main.MainPageViewModel

@Composable
fun MainPage(
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onClearSettings: () -> Unit,
    /*onAddEventClick: (LocalDate) -> Unit,*/
) {
    var weekSelectorExpanded by remember { mutableStateOf(false) }
    var eventCreateExpanded by remember { mutableStateOf(false) }
    var workTypeSelectorExpanded by remember { mutableStateOf(false) }
    var updateInfoExpanded by remember { mutableStateOf(false) }
    val settings by rememberSettings()
    var isScheduleRefreshing by remember { mutableStateOf(false) }

    val viewModel: MainPageViewModel = viewModel(
        factory = MainPageViewModel.Factory(
            onClearSettings = onClearSettings,
            onShowUpdateInfo = {
                updateInfoExpanded = true
            },
        )
    )
    val accountViewModel: AccountViewModel = viewModel(factory = AccountViewModel.Factory())

    LaunchedEffect(settings.selectedSchedule) {
        viewModel.reloadSchedule(settings.selectedSchedule)
    }
    val viewState by viewModel.state.collectAsState()
    val requestRefresh = { viewModel.reloadSchedule(settings.selectedSchedule) }
    val onAddEventClick: (LocalDate) -> Unit = { eventCreateExpanded = true }

    Column {
        MainPageNavigation(
            page = viewState.page,
            setPage = { page -> viewModel.setPage(page) },
            header = { page ->
                when (page) {
                    MainNavigationPageId.INFORMATION -> MainPageHomeTitle(
                        title = stringResource(Res.string.information),
                        schedule = viewState.schedule,
                        onRequestRefresh = requestRefresh,
                    )

                    MainNavigationPageId.WORKS -> MainPageHomeTitle(
                        title = stringResource(Res.string.works),
                        schedule = viewState.schedule,
                        buttonText = stringResource(Res.string.work_type),
                        onButtonClick = { workTypeSelectorExpanded = !workTypeSelectorExpanded },
                        onRequestRefresh = requestRefresh,
                    )

                    MainNavigationPageId.HOME -> MainPageHomeTitle(
                        title = stringResource(Res.string.schedule),
                        schedule = viewState.schedule,
                        buttonText = stringResource(Res.string.select_week),
                        onButtonClick = { weekSelectorExpanded = true },
                        onRequestRefresh = requestRefresh,
                    )

                    MainNavigationPageId.ACCOUNT -> MainPageHomeTitle(
                        title = stringResource(Res.string.account),
                        schedule = viewState.schedule,
                        onRequestRefresh = requestRefresh,
                    )

                    MainNavigationPageId.SETTINGS -> MainPageHomeTitle(
                        title = stringResource(Res.string.settings),
                        schedule = viewState.schedule,
                        onRequestRefresh = requestRefresh,
                    )
                }
            },
        ) { page ->
            when (page) {
                MainNavigationPageId.INFORMATION -> {
                    LoadableView(state = viewState.maidata, retry = viewModel::startLoading) {
                        MaiDataView(
                            manifest = viewState.maidata.data!!,
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
                            selector = remember (viewState.workLessonSelectors) { viewState.workLessonSelectors.anySelector() }
                        )
                    }
                }

                MainNavigationPageId.HOME -> {
                    LoadableView(state = viewState.schedule, retry = viewModel::startLoading) {
                        ScheduleView(
                            schedule = viewState.schedule.data!!,
                            events = viewState.events.data ?: emptyList(),
                            exlerTeachers = viewState.exlerTeachers.data,
                            dateRange = viewState.selectedWeek,
                            modifier = Modifier.fillMaxSize(),
                            onRefresh = {
                                isScheduleRefreshing = true
                                viewModel.reloadSchedule() {
                                    isScheduleRefreshing = false
                                }
                            },
                            refreshing = isScheduleRefreshing,
                            showEventAddingButton = true,
                            onAddEventClick = onAddEventClick,
                        )
                    }
                }

                MainNavigationPageId.ACCOUNT -> Column(Modifier.fillMaxSize()) {
                    AccountPage(
                        viewModel = accountViewModel,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                MainNavigationPageId.SETTINGS -> Column(Modifier.fillMaxSize()) {
                    SettingsPage(schedule = viewState.schedule)
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

    if (eventCreateExpanded) {
        EventCreateDialog(
            onDismissRequest = { eventCreateExpanded = false },
            initialDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
            onEventCreated = { eventCreateExpanded = false; viewModel.createSimpleEvent(it) },
            scheduleName = viewState.schedule.data?.name ?: "unknown_schedule",
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
