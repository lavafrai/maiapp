package ru.lavafrai.maiapp.theme.themes

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamicColorScheme
import ru.lavafrai.maiapp.theme.ApplicationTheme
import ru.lavafrai.maiapp.theme.MaiColor


class DefaultLightTheme: ApplicationTheme() {
    override val id: String = "light"
    override val readableName: String = "Light"

    @Composable
    override fun isDark(): Boolean = false

    @Composable
    override fun colorScheme(): ColorScheme {
        return dynamicColorScheme(
            primary = MaiColor,
            isDark = false,
            style = PaletteStyle.Vibrant,
            isAmoled = false,
        )
    }
}
