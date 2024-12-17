package ru.lavafrai.maiapp.navigation

import androidx.compose.animation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.toRoute
import ru.lavafrai.maiapp.rootPages.login.GreetingPage
import ru.lavafrai.maiapp.rootPages.login.LoginPage
import ru.lavafrai.maiapp.rootPages.teacherReviewsPage.TeacherReviewsPage
import ru.lavafrai.maiapp.data.settings.getSettings
import ru.lavafrai.maiapp.navigation.pages.GreetingPage
import ru.lavafrai.maiapp.navigation.pages.LoginPage
import ru.lavafrai.maiapp.navigation.pages.MainPage
import ru.lavafrai.maiapp.navigation.pages.TeacherReviewsPage
import ru.lavafrai.maiapp.rootPages.main.MainPage
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
                startDestination = if (settings.hasSelectedGroup()) MainPage else GreetingPage,
                popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
                popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) },
                enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
                exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) },
            ) {
                composable<GreetingPage> { backStackEntry ->
                    GreetingPage(
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedContentScope = this@composable,
                        onLoginAsStudent = { navController.navigate(LoginPage(LoginType.STUDENT, LoginTarget.ADD_SCHEDULE, true)) },
                        onLoginAsTeacher = { navController.navigate(LoginPage(LoginType.TEACHER, LoginTarget.ADD_SCHEDULE, true)) },
                    )
                }

                composable<LoginPage> (
                    typeMap = mapOf(
                        typeOf<LoginType>() to navTypeOf<LoginType>(),
                        typeOf<LoginTarget>() to navTypeOf<LoginTarget>()
                    ),
                ) { backStackEntry ->
                    val loginData: LoginPage = backStackEntry.toRoute()

                    LoginPage(
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedContentScope = this@composable,
                        onNavigateBack = { navController.navigateUp() },
                        onLoginDone = {
                            navController.popBackStack()
                            navController.navigate(MainPage) {
                                popUpTo(0)
                            }
                        },
                        loginData = loginData,
                    )
                }

                composable<MainPage> { backStackEntry ->
                    MainPage(
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedContentScope = this@composable,
                        onClearSettings = {
                            navController.navigate(GreetingPage) {
                                popUpTo(0)
                            }
                        },
                    )
                }

                composable<TeacherReviewsPage>(
                    typeMap = mapOf(
                        typeOf<LoginType>() to navTypeOf<LoginType>(),
                        typeOf<LoginTarget>() to navTypeOf<LoginTarget>()
                    ),
                ) { backStackEntry ->
                    val loginData: TeacherReviewsPage = backStackEntry.toRoute()

                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        TeacherReviewsPage(
                            //sharedTransitionScope = this@SharedTransitionLayout,
                            //animatedContentScope = this@dialog,
                            onNavigateBack = { navController.navigateUp() },
                            teacherId = loginData.teacherId,
                        )
                    }
                }
            }
        }
    }
}