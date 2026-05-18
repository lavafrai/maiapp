package ru.lavafrai.maiapp.theme

import androidx.compose.runtime.Composable

interface ApplicationTheme {
    val id: String
    val readableName: @Composable () -> String

    @Composable
    fun isDark(): Boolean

    @Composable
    fun isAmoled(): Boolean
}