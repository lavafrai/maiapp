package ru.lavafrai.maiapp.theme.themes

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import ru.lavafrai.maiapp.theme.ApplicationTheme


class SystemTheme: ApplicationTheme {
    override val id = "system"
    override val readableName = @Composable { "System" }

    @Composable
    override fun isDark(): Boolean = isSystemInDarkTheme()

    @Composable
    override fun isAmoled() = false
}
