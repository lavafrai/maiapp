package ru.lavafrai.maiapp

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.navigation.compose.rememberNavController
import com.dokar.sonner.Toaster
import com.dokar.sonner.rememberToasterState
import ru.lavafrai.maiapp.fragments.ripleLoadingIndicator.RippleLoadingIndicator
import ru.lavafrai.maiapp.fragments.slideButton.SlideButton
import ru.lavafrai.maiapp.navigation.AppNavigation
import ru.lavafrai.maiapp.navigation.pages.GreetingPage
import ru.lavafrai.maiapp.platform.getPlatform
import ru.lavafrai.maiapp.theme.AppTheme
import ru.lavafrai.maiapp.theme.LocalApplicationTheme

@Composable
internal fun App() = AppTheme {
    val platform = remember { getPlatform() }
    val navController = rememberNavController()
    val toaster = rememberToasterState()
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

    Toaster(
        state = toaster,
        alignment = Alignment.BottomCenter,
        darkTheme = LocalApplicationTheme.current.value.isDark(),
    )
}
