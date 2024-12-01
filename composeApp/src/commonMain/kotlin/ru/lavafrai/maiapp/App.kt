package ru.lavafrai.maiapp

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.dokar.sonner.Toaster
import com.dokar.sonner.rememberToasterState
import ru.lavafrai.maiapp.navigation.AppNavigation
import ru.lavafrai.maiapp.navigation.pages.GreetingPage
import ru.lavafrai.maiapp.platform.getPlatformSettingsStorage
import ru.lavafrai.maiapp.theme.AppTheme
import ru.lavafrai.maiapp.theme.LocalApplicationTheme

@Composable
internal fun App() = AppTheme {
    val navController = rememberNavController()
    val toaster = rememberToasterState()
    val applicationContext = ApplicationContext(
        panicCleanup = {
            navController.navigate(GreetingPage) { popUpTo(0) }
            getPlatformSettingsStorage().clear()
        },
    )

    CompositionLocalProvider(LocalApplicationContext provides applicationContext) {
        AppNavigation(
            navController = navController,
            modifier = Modifier
                .fillMaxSize(),
        )
    }

    Toaster(
        state = toaster,
        alignment = Alignment.BottomCenter,
        darkTheme = LocalApplicationTheme.current.value.isDark(),
    )
}
