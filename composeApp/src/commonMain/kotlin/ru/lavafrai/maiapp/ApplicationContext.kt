package ru.lavafrai.maiapp

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavController
import ru.lavafrai.maiapp.models.schedule.Lesson
import ru.lavafrai.maiapp.models.schedule.Schedule
import ru.lavafrai.maiapp.models.schedule.ScheduleId
import ru.lavafrai.maiapp.models.schedule.TeacherUid
import ru.lavafrai.maiapp.navigation.pages.*
import ru.lavafrai.maiapp.utils.UrlHandler

class ApplicationContext(
    val navController: NavController,
    val panicCleanup: () -> Unit,
    val safeCleanup: () -> Unit = panicCleanup,
) {
    val urlHandler: UrlHandler = UrlHandler(this)

    fun openTeacherReviews(teacherId: String, teacherUid: TeacherUid) {
        navController.navigate(TeacherReviewsPage(teacherId, teacherUid))
    }

    fun openDonations() {
        UrlHandler(this).openUrl("https://maiapp.lavafrai.ru/donate")
    }

    fun openSchedule(scheduleId: ScheduleId, title: String? = null) {
        navController.navigate(DedicatedSchedulePage(scheduleId, title))
    }

    fun requestSafeDataClean() {
        navController.navigate(SafeDataCleanup)
    }

    fun openUrl(
        url: String,
        forcedInBrowser: Boolean = false,
    ) {
        urlHandler.openUrl(url, forcedInBrowser)
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