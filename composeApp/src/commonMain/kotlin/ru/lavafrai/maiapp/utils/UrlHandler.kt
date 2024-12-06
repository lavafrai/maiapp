package ru.lavafrai.maiapp.utils

import androidx.compose.runtime.Composable
import ru.lavafrai.maiapp.ApplicationContext
import ru.lavafrai.maiapp.LocalApplicationContext
import ru.lavafrai.maiapp.data.repositories.ExlerRepository
import ru.lavafrai.maiapp.models.exler.ExlerTeacher
import ru.lavafrai.maiapp.platform.getPlatform

class UrlHandler(
    val applicationContext: ApplicationContext,
) {
    fun openUrl(url: String) {
        if (url.startsWith("https://mai-exler.ru")) {
            if (tryToOpenExlerInApp(url)) return
        }

        val platform = getPlatform()
        platform.openUrl(url)
    }

    private fun tryToOpenExlerInApp(url: String): Boolean {
        if (!url.startsWith("https://mai-exler.ru/prepods")) return false
        val teachers = ExlerRepository().getTeachersFromCacheOrNull() ?: return false
        val teacherId = teachers.find { "https://mai-exler.ru${it.path}" == url } ?: return false
        openTeacherInfo(teacherId)
        return true
    }

    private fun openTeacherInfo(teacher: ExlerTeacher) {
        applicationContext.openTeacherReviews(teacher.name)
    }

    companion object {
        @Composable
        fun create(): UrlHandler {
            val context = LocalApplicationContext.current
            return UrlHandler(context)
        }
    }
}