package ru.lavafrai.maiapp.utils

import androidx.compose.runtime.Composable
import ru.lavafrai.maiapp.ApplicationContext
import ru.lavafrai.maiapp.LocalApplicationContext
import ru.lavafrai.maiapp.data.repositories.ExlerRepository
import ru.lavafrai.maiapp.models.exler.ExlerTeacher
import ru.lavafrai.maiapp.models.exler.ReviewsSite
import ru.lavafrai.maiapp.models.schedule.TeacherUid
import ru.lavafrai.maiapp.platform.getPlatform

class UrlHandler(
    val applicationContext: ApplicationContext,
) {
    fun openUrl(
        url: String,
        forcedInBrowser: Boolean = false,
    ) {
        if (!forcedInBrowser) {
            val siteUrl = ReviewsSite.urls.find { url.startsWith(it) }
            if (siteUrl != null && tryToOpenTeacherInApp(url, siteUrl)) return
        }

        val platform = getPlatform()
        platform.openUrl(url)
    }

    private fun tryToOpenTeacherInApp(url: String, siteUrl: String): Boolean {
        if (!url.startsWith("$siteUrl/prepods")) return false
        val teachers = ExlerRepository().getTeachersFromCacheOrNull() ?: return false
        val teacherId = teachers.find { "$siteUrl${it.path}" == url } ?: return false
        openTeacherInfo(teacherId, TeacherUid.Empty)
        return true
    }

    private fun openTeacherInfo(teacher: ExlerTeacher, teacherUid: TeacherUid) {
        applicationContext.openTeacherReviews(teacher.name, teacherUid)
    }

    companion object {
        @Composable
        fun create(): UrlHandler {
            val context = LocalApplicationContext.current
            return UrlHandler(context)
        }
    }
}
