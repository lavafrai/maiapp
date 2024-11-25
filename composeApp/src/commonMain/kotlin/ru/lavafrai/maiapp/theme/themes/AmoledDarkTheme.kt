package ru.lavafrai.maiapp.theme.themes

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamicColorScheme
import ru.lavafrai.maiapp.theme.ApplicationTheme
import ru.lavafrai.maiapp.theme.MaiColor

class AmoledDarkTheme: ApplicationTheme() {
    override val id: String = "amoled"
    override val readableName: String = "Amoled"

    @Composable
    override fun isDark(): Boolean = true

    @Composable
    override fun colorScheme(): ColorScheme {
        return dynamicColorScheme(
            primary = MaiColor,
            isDark = true,
            style = PaletteStyle.Vibrant,
            isAmoled = true,
        )
    }
}
