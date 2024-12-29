@file:OptIn(ExperimentalMaterial3WindowSizeClassApi::class)

package ru.lavafrai.maiapp.rootPages.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import compose.icons.FeatherIcons
import compose.icons.feathericons.*
import ru.lavafrai.maiapp.utils.MainNavigationPageIdSaver
import soup.compose.material.motion.MaterialMotion
import soup.compose.material.motion.animation.materialSharedAxisX
import soup.compose.material.motion.animation.rememberSlideDistance

val mainNavigationItems = listOf(
    MainNavigationItem(
        id = MainNavigationPageId.INFORMATION,
        icon = { Icon(FeatherIcons.Info, "Info") },
        title = { Text("Info") },
    ),
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
        id = MainNavigationPageId.ACCOUNT,
        icon = { Icon(FeatherIcons.User, "Account") },
        title = { Text("Account") },
    ),
    MainNavigationItem(
        id = MainNavigationPageId.SETTINGS,
        icon = { Icon(FeatherIcons.Settings, "Settings") },
        title = { Text("Settings") },
    ),
)

@Composable
fun MainPageNavigation(
    page: MainNavigationPageId,
    setPage: (MainNavigationPageId) -> Unit,
    header: @Composable (MainNavigationPageId) -> Unit,
    content: @Composable (MainNavigationPageId) -> Unit,
) {
    // var selected by rememberSaveable(saver = MainNavigationPageIdSaver, key = "main-page-selected") { mutableStateOf(MainNavigationPageId.HOME) }
    val selectedItem by remember(page) { mutableStateOf(mainNavigationItems.find { it.id == page }!!) }

    val navRail = @Composable {
        NavigationRail(
            windowInsets = WindowInsets.safeDrawing
        ) {
            Column(Modifier.fillMaxHeight(), verticalArrangement = Arrangement.Center) {
                mainNavigationItems.forEachIndexed { index, item ->
                    NavigationRailItem(
                        icon = item.icon,
                        label = item.title,
                        onClick = { setPage(mainNavigationItems[index].id) },
                        selected = selectedItem.id == item.id,
                    )
                }
            }
        }
    }
    val navBar = @Composable {
        NavigationBar (

        ) {
            mainNavigationItems.forEachIndexed { index, item ->
                NavigationBarItem(
                    icon = item.icon,
                    // label = item.title,
                    onClick = { setPage(mainNavigationItems[index].id) },
                    selected = selectedItem.id == item.id,
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
                header(selectedItem.id)
                MaterialMotion(
                    targetState = selectedItem.id,
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
