@file:OptIn(ExperimentalMaterial3WindowSizeClassApi::class)

package ru.lavafrai.maiapp.navigation.rootPages.main

import androidx.compose.animation.togetherWith
import androidx.compose.animation.with
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import compose.icons.FeatherIcons
import compose.icons.feathericons.Award
import compose.icons.feathericons.Home
import compose.icons.feathericons.Settings
import soup.compose.material.motion.MaterialMotion
import soup.compose.material.motion.animation.*

val mainNavigationItems = listOf(
    MainNavigationItem(
        id = MainNavigationPageId.WORKS,
        icon = { Icon(FeatherIcons.Award, "Works") },
        title = { Text("Works") },
    ),
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
    header: @Composable (MainNavigationPageId) -> Unit,
    content: @Composable (MainNavigationPageId) -> Unit,
) {
    var selected by remember { mutableStateOf(mainNavigationItems.find { it.id == MainNavigationPageId.HOME }!!) }
    val navRail = @Composable {
        NavigationRail {
            Spacer(Modifier.height(8.dp))
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
    val slideDistance = rememberSlideDistance(slideDistance = 30.dp)

    Row {
        if (wideScreen) navRail()
        Column {
            Column(modifier = Modifier.weight(1f)) {
                header(selected.id)
                MaterialMotion(
                    targetState = selected.id,
                    transitionSpec = {
                        materialSharedAxisX(forward = true, slideDistance = slideDistance)
                    },
                ) { page ->
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        content(page)
                    }
                }
            }
            if (!wideScreen) navBar()
        }
    }
}
