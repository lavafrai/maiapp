package ru.lavafrai.maiapp.theme

import androidx.compose.runtime.Composable

interface ApplicationTheme {
    abstract val id: String
    abstract val readableName: @Composable () -> String

    @Composable
    abstract fun isDark(): Boolean

    @Composable
    abstract fun isAmoled(): Boolean
}