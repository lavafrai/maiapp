package ru.lavafrai.maiapp.rootPages.main

import androidx.compose.runtime.Composable
import kotlinx.serialization.Serializable

@Serializable
enum class MainNavigationPageId {
    INFORMATION,
    WORKS,
    HOME,
    ACCOUNT,
    SETTINGS,
}

@Serializable
data class MainNavigationItem(
    val id: MainNavigationPageId,
    val icon: @Composable (selected: Boolean) -> Unit,
    val title: @Composable () -> Unit,
)