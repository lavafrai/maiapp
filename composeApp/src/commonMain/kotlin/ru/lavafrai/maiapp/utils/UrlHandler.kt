package ru.lavafrai.maiapp.utils

import androidx.compose.runtime.Composable
import ru.lavafrai.maiapp.ApplicationContext
import ru.lavafrai.maiapp.LocalApplicationContext
import ru.lavafrai.maiapp.data.repositories.ExlerRepository
import ru.lavafrai.maiapp.models.exler.ExlerTeacher
import ru.lavafrai.maiapp.models.schedule.TeacherUid
import ru.lavafrai.maiapp.platform.getPlatform

class UrlHandler(
    val applicationContext: ApplicationContext,
) {
    fun openUrl(
        url: String,
        forcedInBrowser: Boolean = false,
    ) {
        if (url.startsWith("https://mai-exler.ru") && !forcedInBrowser) {
            if (tryToOpenExlerInApp(url)) return
        }

        val platform = getPlatform()
        platform.openUrl(url)
    }

    private fun tryToOpenExlerInApp(url: String): Boolean {
        if (!url.startsWith("https://mai-exler.ru/prepods")) return false
        val teachers = ExlerRepository().getTeachersFromCacheOrNull() ?: return false
        val teacherId = teachers.find { "https://mai-exler.ru${it.path}" == url } ?: return false
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