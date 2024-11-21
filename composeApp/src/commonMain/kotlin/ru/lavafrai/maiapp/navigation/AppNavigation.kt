package ru.lavafrai.maiapp.navigation

import androidx.compose.animation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import ru.lavafrai.maiapp.navigation.rootPages.login.GreetingPage
import ru.lavafrai.maiapp.navigation.rootPages.login.LoginPage
import ru.lavafrai.maiapp.data.getSettings

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    val settings = getSettings()
    Box(
        modifier = modifier,
    ) {
        SharedTransitionLayout {
            NavHost(
                navController = navController,
                startDestination = if (settings.hasSelectedGroup()) Pages.Main else Pages.Greeting,
            ) {
                composable<Pages.Greeting> (
                    enterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
                    exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) },
                ) {
                    GreetingPage(
                        navController = navController,
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedContentScope = this@composable,
                    )
                }

                composable<Pages.Login> (
                    enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
                    exitTransition = { slideOutHorizontally(targetOffsetX = { it }) },
                ) { backStackEntry ->
                    val loginData: Pages.Login = backStackEntry.toRoute()

                    LoginPage(
                        navController = navController,
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedContentScope = this@composable,
                        loginData = loginData,
                    )
                }
            }
        }
    }
}