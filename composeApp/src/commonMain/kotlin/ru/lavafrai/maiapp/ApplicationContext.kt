package ru.lavafrai.maiapp

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavController
import ru.lavafrai.maiapp.navigation.pages.TeacherReviewsPage

class ApplicationContext(
    val navController: NavController,
    val panicCleanup: () -> Unit,
) {
    fun openTeacherReviews(teacherId: String) {
        navController.navigate(TeacherReviewsPage(teacherId))
    }
}

val LocalApplicationContext = staticCompositionLocalOf<ApplicationContext> {
    error("No ApplicationContext provided")
}