package ru.lavafrai.maiapp.navigation

import androidx.compose.animation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.toRoute
import ru.lavafrai.maiapp.LocalApplicationContext
import ru.lavafrai.maiapp.data.settings.getSettings
import ru.lavafrai.maiapp.fragments.SafeDataCleanupView
import ru.lavafrai.maiapp.fragments.schedule.LessonDetailsDialog
import ru.lavafrai.maiapp.models.schedule.Lesson
import ru.lavafrai.maiapp.models.schedule.Schedule
import ru.lavafrai.maiapp.models.schedule.ScheduleId
import ru.lavafrai.maiapp.navigation.pages.*
import ru.lavafrai.maiapp.rootPages.imageViewPage.ImageViewPage
import ru.lavafrai.maiapp.rootPages.login.GreetingPage
import ru.lavafrai.maiapp.rootPages.login.LoginPage
import ru.lavafrai.maiapp.rootPages.main.MainPage
import ru.lavafrai.maiapp.rootPages.schedule.DedicatedSchedulePage
import ru.lavafrai.maiapp.rootPages.teacherReviewsPage.TeacherReviewsPage
import ru.lavafrai.maiapp.viewmodels.login.LoginTarget
import ru.lavafrai.maiapp.viewmodels.login.LoginType
import kotlin.reflect.typeOf

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    val appContext = LocalApplicationContext.current
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
                        typeOf<LoginTarget>() to navTypeOf<LoginTarget>(),
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

                composable<DedicatedSchedulePage>(
                    typeMap = mapOf(
                        typeOf<ScheduleId>() to navTypeOf<ScheduleId>(),
                    ),
                ) { backStackEntry ->
                    val scheduleId: ScheduleId = backStackEntry.toRoute<DedicatedSchedulePage>().scheduleId

                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        DedicatedSchedulePage(
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedContentScope = this@composable,
                            onNavigateBack = { navController.navigateUp() },
                            scheduleId = scheduleId,
                        )
                    }

                }

                dialog<SafeDataCleanup> {
                    SafeDataCleanupView(
                        onNavigateBack = { navController.navigateUp() },
                        onClean = {
                            appContext.safeCleanup()
                            // navController.navigateUp()
                        },
                    )
                }

                dialog<LessonDetailsPage>(
                    typeMap = mapOf(
                        typeOf<Lesson>() to navTypeOf<Lesson>(),
                        typeOf<Schedule>() to navTypeOf<Schedule>(),
                    ),
                ) { backStackEntry ->
                    val lesson = (backStackEntry.toRoute() as LessonDetailsPage).lesson
                    val schedule = (backStackEntry.toRoute() as LessonDetailsPage).schedule

                    LessonDetailsDialog(
                        onNavigateBack = { navController.navigateUp() },
                        lesson = lesson,
                        schedule = schedule,
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
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedContentScope = this@composable,
                            onNavigateBack = { navController.navigateUp() },
                            teacherId = loginData.teacherId,
                        )
                    }
                }

                composable<ImageViewPage>(
                    enterTransition = { fadeIn() },
                    exitTransition = { fadeOut() },
                    popExitTransition = { fadeOut() },
                    popEnterTransition = { fadeIn() },
                ) { backStackEntry ->
                    val imageData: ImageViewPage = backStackEntry.toRoute()

                    ImageViewPage(
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedContentScope = this@composable,
                        url = imageData.url,
                        onNavigateBack = { navController.navigateUp() },
                    )
                }
            }
        }
    }
}