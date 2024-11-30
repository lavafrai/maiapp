package ru.lavafrai.maiapp.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

interface ApplicationColorSchema {
    val id: String
    val readableName: @Composable () -> String

    @Composable
    fun buildColorScheme(theme: ApplicationTheme): ColorScheme
}