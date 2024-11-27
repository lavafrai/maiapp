package ru.lavafrai.maiapp

import androidx.compose.runtime.staticCompositionLocalOf

class ApplicationContext(
    val panicCleanup: () -> Unit,
) {

}

val LocalApplicationContext = staticCompositionLocalOf<ApplicationContext> {
    error("No ApplicationContext provided")
}