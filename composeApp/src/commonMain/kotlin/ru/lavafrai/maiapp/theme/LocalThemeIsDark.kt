package ru.lavafrai.maiapp.theme

import androidx.compose.runtime.compositionLocalOf

val LocalThemeIsDark = compositionLocalOf<Boolean> { error("No preferred color theme set") }