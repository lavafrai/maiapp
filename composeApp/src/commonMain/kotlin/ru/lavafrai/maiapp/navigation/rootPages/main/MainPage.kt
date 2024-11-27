@file:OptIn(ExperimentalSharedTransitionApi::class)

package ru.lavafrai.maiapp.navigation.rootPages.main

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import maiapp.composeapp.generated.resources.Res
import maiapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import ru.lavafrai.maiapp.data.settings.rememberSettings
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
        MainPageTitle(
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
        )

        Spacer(modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars))
        Row {
            Button(onClick = { viewModel.clearSettings() }) {
                Text("Clear settings")
            }
        }
        ThemeSelectButton { themeId ->
            viewModel.setTheme(themeId)
        }

        Text("Schedule: ${viewState.schedule.status}")
        Text("Week: ${viewState.selectedWeek}")


        if (viewState.schedule.hasData()) ScheduleView(
            schedule = viewState.schedule.data!!,
            dateRange = viewState.selectedWeek,
            modifier = Modifier.fillMaxSize().weight(1f),
        )
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
