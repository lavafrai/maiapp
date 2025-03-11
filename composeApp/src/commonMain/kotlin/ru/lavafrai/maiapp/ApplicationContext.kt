package ru.lavafrai.maiapp

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavController
import ru.lavafrai.maiapp.models.maidata.MaiDataItem
import ru.lavafrai.maiapp.models.maidata.MaiDataItemType
import ru.lavafrai.maiapp.models.maidata.asset.TextAsset
import ru.lavafrai.maiapp.models.maidata.asset.WebviewAsset
import ru.lavafrai.maiapp.models.schedule.Lesson
import ru.lavafrai.maiapp.models.schedule.Schedule
import ru.lavafrai.maiapp.models.schedule.ScheduleId
import ru.lavafrai.maiapp.models.schedule.TeacherUid
import ru.lavafrai.maiapp.navigation.pages.*
import ru.lavafrai.maiapp.utils.UrlHandler
import ru.lavafrai.maiapp.utils.resolveAsset
import ru.lavafrai.maiapp.viewmodels.login.LoginTarget
import ru.lavafrai.maiapp.viewmodels.login.LoginType

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

    private fun openBuiltinMaiData(item: MaiDataItem) {
        require(item.type == MaiDataItemType.Builtin)
        require(item.asset != null)
        require(item.asset is TextAsset)

        val intent = when ((item.asset as TextAsset).text) {
            "teacher-schedule" -> LoginPage(type = LoginType.TEACHER, target = LoginTarget.OPEN_SCHEDULE)
            "teacher-review" -> LoginPage(type = LoginType.EXLER, target = LoginTarget.OPEN_SCHEDULE)
            else -> return
        }

        navController.navigate(intent)
    }

    private fun openWebView(url: String, title: String) {
        navController.navigate(WebViewPage(url = url, title = title))
    }

    private fun openMapView(url: String, title: String) {
        navController.navigate(MapPage(url = url, title = title))
    }

    fun openMaiDataItem(item: MaiDataItem, night: Boolean) = when (item.type) {
        MaiDataItemType.Builtin -> openBuiltinMaiData(item)
        MaiDataItemType.Web -> {
            require(item.resolveAsset(night) is WebviewAsset)
            val asset = item.resolveAsset(night) as WebviewAsset
            val title = item.name
            openWebView("${BuildConfig.API_BASE_URL}/assets/${asset.text}", title)
        }
        MaiDataItemType.Map -> {
            require(item.resolveAsset(night) is WebviewAsset)
            val asset = item.resolveAsset(night) as WebviewAsset
            val title = item.name
            openMapView("${BuildConfig.API_BASE_URL}/assets/${asset.text}", title)
        }
        MaiDataItemType.DoNothing -> {

        }
    }
}

val LocalApplicationContext = staticCompositionLocalOf<ApplicationContext> {
    error("No ApplicationContext provided")
}