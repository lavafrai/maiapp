package ru.lavafrai.maiapp.theme.colorSchemas

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamicColorScheme
import maiapp.composeapp.generated.resources.Res
import maiapp.composeapp.generated.resources.classic_color_scheme
import org.jetbrains.compose.resources.stringResource
import ru.lavafrai.maiapp.theme.ApplicationColorSchema
import ru.lavafrai.maiapp.theme.ApplicationTheme
import ru.lavafrai.maiapp.theme.MaiColor

class ClassicColorSchema: ApplicationColorSchema {
    override val id = "classic"
    override val readableName = @Composable { stringResource(Res.string.classic_color_scheme) }

    @Composable
    override fun buildColorScheme(theme: ApplicationTheme): ColorScheme {
        return dynamicColorScheme(
            seedColor = MaiColor,
            isDark = theme.isDark(),
            isAmoled = theme.isAmoled(),
            style = PaletteStyle.Monochrome,
        )
    }
}