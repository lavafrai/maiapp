package ru.lavafrai.maiapp.theme.themes

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamicColorScheme
import ru.lavafrai.maiapp.theme.ApplicationTheme
import ru.lavafrai.maiapp.theme.MaiColor

class AmoledDarkTheme: ApplicationTheme {
    override val id = "amoled"
    override val readableName = @Composable { "Amoled" }

    @Composable
    override fun isDark(): Boolean = true

    @Composable
    override fun isAmoled(): Boolean = true
}
