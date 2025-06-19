package ru.lavafrai.maiapp.theme

import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import ru.lavafrai.maiapp.data.settings.rememberSettings


internal val LocalApplicationTheme =
    compositionLocalOf<State<ApplicationTheme>> { error("No theme provided") }
internal val LocalApplicationColorSchema =
    compositionLocalOf<State<ApplicationColorSchema>> { error("No color schema provided") }

@Composable
internal fun AppTheme(
    content: @Composable () -> Unit
) {
    val settings by rememberSettings()
    val themeProvider = ThemeProvider

    val themeId = settings.theme
    val colorSchemaId = settings.colorSchema

    val theme = themeProvider.findThemeById(themeId)
    val colorSchema = themeProvider.colorSchemas.find { it.id == colorSchemaId }
        ?: themeProvider.defaultColorSchema

    CompositionLocalProvider(
        LocalApplicationTheme provides mutableStateOf(theme),
        LocalApplicationColorSchema provides mutableStateOf(colorSchema),
        LocalThemeIsDark provides theme.isDark()
    ) {
        SystemAppearance(!theme.isDark())

        val colorScheme = colorSchema.buildColorScheme(theme)

        MaterialExpressiveTheme(
            colorScheme = colorScheme.animated(),
            content = { Surface(content = content) }
        )
    }
}

@Composable
internal expect fun SystemAppearance(isDark: Boolean)
