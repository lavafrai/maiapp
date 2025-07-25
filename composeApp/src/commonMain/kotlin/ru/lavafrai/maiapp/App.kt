package ru.lavafrai.maiapp

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import ru.lavafrai.maiapp.navigation.AppNavigation
import ru.lavafrai.maiapp.navigation.pages.GreetingPage
import ru.lavafrai.maiapp.platform.getPlatform
import ru.lavafrai.maiapp.theme.AppTheme

@Composable
internal fun App() = AppTheme {
    val platform = remember { getPlatform() }
    val navController = rememberNavController()
    val applicationContext = remember { ApplicationContext(
        navController = navController,
        panicCleanup = {
            navController.navigate(GreetingPage) { popUpTo(0) }
            platform.storage().clear()
        },
    )}

    CompositionLocalProvider(LocalApplicationContext provides applicationContext) {
        AppNavigation(
            navController = navController,
            modifier = Modifier
                .fillMaxWidth(),
        )
    }
}
