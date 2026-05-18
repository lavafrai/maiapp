package ru.lavafrai.maiapp.theme.themes

import androidx.compose.runtime.Composable
import ru.lavafrai.maiapp.theme.ApplicationTheme


class DefaultDarkTheme: ApplicationTheme {
    override val id = "dark"
    override val readableName = @Composable { "Dark" }

    @Composable
    override fun isDark() = true

    @Composable
    override fun isAmoled() = false
}
