package ru.lavafrai.maiapp.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

abstract class ApplicationTheme {
    abstract val id: String
    abstract val readableName: String

    @Composable
    abstract fun isDark(): Boolean

    @Composable
    abstract fun colorScheme(): ColorScheme
}