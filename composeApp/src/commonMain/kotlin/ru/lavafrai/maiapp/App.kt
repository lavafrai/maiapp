package ru.lavafrai.maiapp

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import ru.lavafrai.maiapp.navigation.AppNavigation
import ru.lavafrai.maiapp.theme.AppTheme

@Composable
internal fun App() = AppTheme {
    val navController = rememberNavController()

    AppNavigation(
        navController = navController,
        modifier = Modifier
            .fillMaxSize(),
    )
}
