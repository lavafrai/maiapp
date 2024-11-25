package ru.lavafrai.maiapp.theme

import ru.lavafrai.maiapp.theme.themes.*

object ThemeProvider {
    val themes = listOf(
        DefaultLightTheme(),
        SystemTheme(),
        DefaultDarkTheme(),
        AmoledDarkTheme(),
        ClassicDarkTheme(),
    )

    val defaultTheme = SystemTheme()

    fun findById(id: String): ApplicationTheme? {
        return themes.find { it.id == id }
    }
}