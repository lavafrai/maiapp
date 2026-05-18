package ru.lavafrai.maiapp.theme.themes

import androidx.compose.runtime.Composable
import ru.lavafrai.maiapp.theme.ApplicationTheme


class DefaultLightTheme: ApplicationTheme {
    override val id = "light"
    override val readableName = @Composable { "Light" }

    @Composable
    override fun isDark(): Boolean = false

    @Composable
    override fun isAmoled() = false
}
