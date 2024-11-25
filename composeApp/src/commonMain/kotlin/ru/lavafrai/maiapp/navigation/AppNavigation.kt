package ru.lavafrai.maiapp.navigation

import androidx.compose.animation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import ru.lavafrai.maiapp.data.settings.ApplicationSettings
import ru.lavafrai.maiapp.navigation.rootPages.login.GreetingPage
import ru.lavafrai.maiapp.navigation.rootPages.login.LoginPage
import ru.lavafrai.maiapp.data.settings.getSettings
import ru.lavafrai.maiapp.navigation.rootPages.main.MainPage
import ru.lavafrai.maiapp.viewmodels.login.LoginTarget
import ru.lavafrai.maiapp.viewmodels.login.LoginType
import kotlin.reflect.typeOf

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    val settings = remember { getSettings() }
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
                ) { backStackEntry ->
                    GreetingPage(
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedContentScope = this@composable,
                        onLoginAsStudent = { navController.navigate(Pages.Login(LoginType.STUDENT, LoginTarget.ADD_SCHEDULE, true)) },
                        onLoginAsTeacher = { navController.navigate(Pages.Login(LoginType.TEACHER, LoginTarget.ADD_SCHEDULE, true)) },
                    )
                }

                composable<Pages.Login> (
                    typeMap = mapOf(
                        typeOf<LoginType>() to navTypeOf<LoginType>(),
                        typeOf<LoginTarget>() to navTypeOf<LoginTarget>()
                    ),
                    enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
                    exitTransition = { slideOutHorizontally(targetOffsetX = { it }) },
                ) { backStackEntry ->
                    val loginData: Pages.Login = backStackEntry.toRoute()

                    LoginPage(
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedContentScope = this@composable,
                        onNavigateBack = { navController.navigateUp() },
                        onLoginDone = {
                            navController.popBackStack()
                            navController.navigate(Pages.Main) {
                                popUpTo(0)
                            }
                        },
                        loginData = loginData,
                    )
                }

                composable<Pages.Main> (
                    enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
                    exitTransition = { slideOutHorizontally(targetOffsetX = { it }) },
                ) { backStackEntry ->
                    MainPage(
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedContentScope = this@composable,
                        onClearSettings = {
                            navController.navigate(Pages.Greeting) {
                                popUpTo(0)
                            }
                        },
                    )
                }
            }
        }
    }
}