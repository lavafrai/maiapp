package ru.lavafrai.maiapp.theme.themes

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import ru.lavafrai.maiapp.theme.ApplicationTheme

class SystemTheme: ApplicationTheme() {
    override val id: String = "system"
    override val readableName: String = "System"

    @Composable
    override fun isDark(): Boolean = isSystemInDarkTheme()

    @Composable
    override fun colorScheme(): ColorScheme {
        return if (isSystemInDarkTheme()) DefaultDarkTheme().colorScheme() else DefaultLightTheme().colorScheme()
    }
}
