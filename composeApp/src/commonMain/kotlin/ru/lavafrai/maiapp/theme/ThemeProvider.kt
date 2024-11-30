package ru.lavafrai.maiapp.theme

import ru.lavafrai.maiapp.theme.colorSchemas.ClassicColorSchema
import ru.lavafrai.maiapp.theme.colorSchemas.DefaultColorSchema
import ru.lavafrai.maiapp.theme.themes.*

object ThemeProvider {
    val themes = listOf(
        DefaultLightTheme(),
        SystemTheme(),
        DefaultDarkTheme(),
        AmoledDarkTheme(),
        // ClassicDarkTheme(),
    )

    val colorSchemas = listOf(
        DefaultColorSchema(),
        ClassicColorSchema(),
    )

    val defaultTheme = SystemTheme()
    val defaultColorSchema = DefaultColorSchema()

    fun findThemeById(id: String): ApplicationTheme {
        return themes.find { it.id == id } ?: defaultTheme
    }

    fun findColorSchemaById(id: String): ApplicationColorSchema {
        return colorSchemas.find { it.id == id } ?: defaultColorSchema
    }
}