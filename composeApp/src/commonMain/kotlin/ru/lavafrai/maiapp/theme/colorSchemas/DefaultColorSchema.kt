package ru.lavafrai.maiapp.theme.colorSchemas

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import com.materialkolor.dynamicColorScheme
import ru.lavafrai.maiapp.theme.ApplicationColorSchema
import ru.lavafrai.maiapp.theme.ApplicationTheme
import ru.lavafrai.maiapp.theme.MaiColor

class DefaultColorSchema: ApplicationColorSchema {
    override val id = "default"
    override val readableName = @Composable { "Default" }

    @Composable
    override fun buildColorScheme(theme: ApplicationTheme): ColorScheme {
        return dynamicColorScheme(
            primary = MaiColor,
            isDark = theme.isDark(),
            isAmoled = theme.isAmoled(),
        )
    }
}