package ru.lavafrai.maiapp.rootPages.main

import androidx.compose.runtime.Composable

enum class MainNavigationPageId {
    WORKS,
    HOME,
    SETTINGS,
}

data class MainNavigationItem(
    val id: MainNavigationPageId,
    val icon: @Composable () -> Unit,
    val title: @Composable () -> Unit,
)