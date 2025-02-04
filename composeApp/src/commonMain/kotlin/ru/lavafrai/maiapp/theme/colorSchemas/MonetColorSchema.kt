package ru.lavafrai.maiapp.theme.colorSchemas

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import ru.lavafrai.maiapp.theme.ApplicationColorSchema
import ru.lavafrai.maiapp.theme.ApplicationTheme

class MonetColorSchema(
    val prebuiltColorSchemaGenerator: @Composable (ApplicationTheme) -> ColorScheme,
): ApplicationColorSchema {
    override val id: String = "monet"
    override val readableName: @Composable () -> String = { "Monet" }

    @Composable
    override fun buildColorScheme(theme: ApplicationTheme): ColorScheme {
        return prebuiltColorSchemaGenerator(theme)
    }
}