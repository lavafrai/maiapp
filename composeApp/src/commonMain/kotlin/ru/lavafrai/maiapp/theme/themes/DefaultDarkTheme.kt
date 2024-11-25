package ru.lavafrai.maiapp.theme.themes

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamicColorScheme
import ru.lavafrai.maiapp.theme.ApplicationTheme
import ru.lavafrai.maiapp.theme.MaiColor

class DefaultDarkTheme: ApplicationTheme() {
    override val id: String = "dark"
    override val readableName: String = "Dark"

    @Composable
    override fun isDark(): Boolean = isSystemInDarkTheme()

    @Composable
    override fun colorScheme(): ColorScheme {
        return dynamicColorScheme(
            primary = MaiColor,
            isDark = true,
            style = PaletteStyle.Vibrant,
            isAmoled = false,
        )
    }
}
