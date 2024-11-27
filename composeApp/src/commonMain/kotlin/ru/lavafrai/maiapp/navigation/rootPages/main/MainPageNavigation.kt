@file:OptIn(ExperimentalMaterial3WindowSizeClassApi::class)

package ru.lavafrai.maiapp.navigation.rootPages.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import compose.icons.FeatherIcons
import compose.icons.feathericons.Home
import compose.icons.feathericons.Settings

val mainNavigationItems = listOf(
    MainNavigationItem(
        id = MainNavigationPageId.HOME,
        icon = { Icon(FeatherIcons.Home, "Home") },
        title = { Text("Home") },
    ),
    MainNavigationItem(
        id = MainNavigationPageId.SETTINGS,
        icon = { Icon(FeatherIcons.Settings, "Settings") },
        title = { Text("Settings") },
    ),
)

@Composable
fun MainPageNavigation(
    content: @Composable (MainNavigationPageId) -> Unit,
) {
    var selected by remember { mutableStateOf(mainNavigationItems[0]) }
    val navRail = @Composable {
        NavigationRail {
            mainNavigationItems.forEachIndexed { index, item ->
                NavigationRailItem(
                    icon = item.icon,
                    label = item.title,
                    onClick = { selected = mainNavigationItems[index] },
                    selected = selected.id == item.id,
                )
            }
        }
    }
    val navBar = @Composable {
        NavigationBar {
            mainNavigationItems.forEachIndexed { index, item ->
                NavigationBarItem(
                    icon = item.icon,
                    label = item.title,
                    onClick = { selected = mainNavigationItems[index] },
                    selected = selected.id == item.id,
                )
            }
        }
    }

    val windowSizeClass = calculateWindowSizeClass()
    val wideScreen = windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact

    Row {
        if (wideScreen) navRail()
        Column {
            Column(modifier = Modifier.weight(1f)) {
                content(selected.id)
            }
            if (!wideScreen) navBar()
        }
    }
}
