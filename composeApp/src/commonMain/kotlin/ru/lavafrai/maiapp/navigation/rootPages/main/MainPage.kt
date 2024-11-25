@file:OptIn(ExperimentalSharedTransitionApi::class)

package ru.lavafrai.maiapp.navigation.rootPages.main

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ru.lavafrai.maiapp.data.settings.ApplicationSettings
import ru.lavafrai.maiapp.data.settings.rememberSettings
import ru.lavafrai.maiapp.fragments.settings.ThemeSelectButton
import ru.lavafrai.maiapp.platform.getPlatformDispatchers
import ru.lavafrai.maiapp.theme.ThemeProvider
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

    Column {
        Spacer(modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars))
        Button(
            onClick = {
                viewModel.clearSettings()
            },
        ) {
            Text("Clear settings")
        }
        ThemeSelectButton { themeId ->
            viewModel.setTheme(themeId)
        }
        Text("Schedule: ${viewState.schedule.status}")
        Text("Data: ${viewState.schedule.data}")
    }
}
