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
import ru.lavafrai.maiapp.fragments.events.EventDetailsDialog
import ru.lavafrai.maiapp.fragments.schedule.LessonDetailsDialog
import ru.lavafrai.maiapp.models.events.RenderedEvent
import ru.lavafrai.maiapp.models.exler.ExlerTeacher
import ru.lavafrai.maiapp.models.schedule.Lesson
import ru.lavafrai.maiapp.models.schedule.Schedule
import ru.lavafrai.maiapp.models.schedule.ScheduleId
import ru.lavafrai.maiapp.models.schedule.TeacherUid
import ru.lavafrai.maiapp.navigation.pages.*
import ru.lavafrai.maiapp.rootPages.imageViewPage.ImageViewPage
import ru.lavafrai.maiapp.rootPages.login.GreetingPage
import ru.lavafrai.maiapp.rootPages.login.LoginPage
import ru.lavafrai.maiapp.rootPages.main.MainPage
import ru.lavafrai.maiapp.rootPages.map.MapPage
import ru.lavafrai.maiapp.rootPages.schedule.DedicatedSchedulePage
import ru.lavafrai.maiapp.rootPages.teacherReviewsPage.TeacherReviewsPage
import ru.lavafrai.maiapp.rootPages.webview.WebViewPage
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
                            when (loginData.target) {
                                LoginTarget.ADD_SCHEDULE -> {
                                    navController.popBackStack()
                                    navController.navigate (MainPage) {
                                        popUpTo(0)
                                    }
                                }
                                LoginTarget.OPEN_SCHEDULE -> {
                                    //require(it is TeacherId)
                                    navController.popBackStack()

                                    if (loginData.type == LoginType.EXLER) {
                                        val exlerTeacher = it as ExlerTeacher

                                        appContext.openTeacherReviews(exlerTeacher.name, TeacherUid.Empty)
                                        return@LoginPage
                                    }
                                    appContext.openSchedule(it as ScheduleId)
                                }
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
                        /*onAddEventClick = { date ->
                            navController.navigate(EventCreatePage(initialDate = date))
                            Logger.d("Add event clicked")
                        }*/
                    )
                }

                composable<DedicatedSchedulePage>(
                    typeMap = mapOf(
                        typeOf<ScheduleId>() to navTypeOf<ScheduleId>(),
                    ),
                ) { backStackEntry ->
                    val route = backStackEntry.toRoute<DedicatedSchedulePage>()
                    val scheduleId: ScheduleId = route.scheduleId

                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        DedicatedSchedulePage(
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedContentScope = this@composable,
                            onNavigateBack = { navController.navigateUp() },
                            scheduleId = scheduleId,
                            title = route.title,
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

                dialog<EventDetailsPage>(
                    typeMap = mapOf(
                        typeOf<RenderedEvent>() to navTypeOf<RenderedEvent>(),
                        typeOf<Schedule>() to navTypeOf<Schedule>(),
                    ),
                ) { backStackEntry ->
                    val event = (backStackEntry.toRoute() as EventDetailsPage).event
                    val schedule = (backStackEntry.toRoute() as EventDetailsPage).schedule

                    EventDetailsDialog(
                        onNavigateBack = { navController.navigateUp() },
                        event = event,
                        schedule = schedule,
                    )
                }

                composable<TeacherReviewsPage>(
                    typeMap = mapOf(
                        typeOf<LoginType>() to navTypeOf<LoginType>(),
                        typeOf<LoginTarget>() to navTypeOf<LoginTarget>(),
                        typeOf<TeacherUid>() to navTypeOf<TeacherUid>(),
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
                            teacherUid = loginData.teacherUid,
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

                composable<WebViewPage>(
                    typeMap = mapOf(
                    ),
                ) { backStackEntry ->
                    val route = backStackEntry.toRoute<WebViewPage>()
                    val url: String = route.url
                    val title: String = route.title

                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        WebViewPage(
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedContentScope = this@composable,
                            onNavigateBack = { navController.navigateUp() },
                            url = url,
                            title = title,
                        )
                    }
                }

                composable<MapPage>(
                    typeMap = mapOf(
                    ),
                ) { backStackEntry ->
                    val route = backStackEntry.toRoute<WebViewPage>()
                    val url: String = route.url
                    val title: String = route.title

                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        MapPage(
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedContentScope = this@composable,
                            onNavigateBack = { navController.navigateUp() },
                            url = url,
                            title = title,
                        )
                    }
                }
            }
        }
    }
}