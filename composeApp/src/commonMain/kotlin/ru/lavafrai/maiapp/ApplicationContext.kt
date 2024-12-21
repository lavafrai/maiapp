package ru.lavafrai.maiapp

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavController
import ru.lavafrai.maiapp.navigation.pages.TeacherReviewsPage
import ru.lavafrai.maiapp.utils.UrlHandler

class ApplicationContext(
    val navController: NavController,
    val panicCleanup: () -> Unit,
) {
    fun openTeacherReviews(teacherId: String) {
        navController.navigate(TeacherReviewsPage(teacherId))
    }

    fun openDonations() {
        UrlHandler(this).openUrl("https://maiapp.lavafrai.ru/donate")
    }
}

val LocalApplicationContext = staticCompositionLocalOf<ApplicationContext> {
    error("No ApplicationContext provided")
}