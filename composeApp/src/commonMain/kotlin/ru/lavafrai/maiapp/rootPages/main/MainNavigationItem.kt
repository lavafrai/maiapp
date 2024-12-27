package ru.lavafrai.maiapp.rootPages.main

import androidx.compose.runtime.Composable
import kotlinx.serialization.Serializable

@Serializable
enum class MainNavigationPageId {
    WORKS,
    HOME,
    SETTINGS,
}

@Serializable
data class MainNavigationItem(
    val id: MainNavigationPageId,
    val icon: @Composable () -> Unit,
    val title: @Composable () -> Unit,
)