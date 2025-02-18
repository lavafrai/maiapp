package ru.lavafrai.maiapp

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.dokar.sonner.Toaster
import com.dokar.sonner.rememberToasterState
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

    Toaster(
        state = toaster,
        //alignment = Alignment.BottomEnd,
        darkTheme = LocalApplicationTheme.current.value.isDark(),
        modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing)
    )

    CompositionLocalProvider(LocalApplicationContext provides applicationContext) {
        AppNavigation(
            navController = navController,
            modifier = Modifier
                .fillMaxWidth(),
        )
    }
}
