package ru.lavafrai.maiapp

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavController
import ru.lavafrai.maiapp.models.schedule.Lesson
import ru.lavafrai.maiapp.models.schedule.Schedule
import ru.lavafrai.maiapp.navigation.pages.ImageViewPage
import ru.lavafrai.maiapp.navigation.pages.LessonDetailsPage
import ru.lavafrai.maiapp.navigation.pages.SafeDataCleanup
import ru.lavafrai.maiapp.navigation.pages.TeacherReviewsPage
import ru.lavafrai.maiapp.utils.UrlHandler

@OptIn(ExperimentalSharedTransitionApi::class)
class ApplicationContext(
    val navController: NavController,
    val panicCleanup: () -> Unit,
    val safeCleanup: () -> Unit = panicCleanup,
) {
    fun openTeacherReviews(teacherId: String) {
        navController.navigate(TeacherReviewsPage(teacherId))
    }

    fun openDonations() {
        UrlHandler(this).openUrl("https://maiapp.lavafrai.ru/donate")
    }

    fun requestSafeDataClean() {
        navController.navigate(SafeDataCleanup)
    }

    fun openImageView(url: String) {
        navController.navigate(ImageViewPage(url = url))
    }

    fun openLessonDetails(
        lesson: Lesson,
        schedule: Schedule,
    ) {
        navController.navigate(LessonDetailsPage(
            lesson = lesson,
            schedule = schedule,
        ))
    }
}

val LocalApplicationContext = staticCompositionLocalOf<ApplicationContext> {
    error("No ApplicationContext provided")
}