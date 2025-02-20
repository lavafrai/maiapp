@file:OptIn(ExperimentalMaterial3WindowSizeClassApi::class)

package ru.lavafrai.maiapp.rootPages.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import maiapp.composeapp.generated.resources.*
import maiapp.composeapp.generated.resources.Res
import maiapp.composeapp.generated.resources.ic_award
import maiapp.composeapp.generated.resources.ic_info
import maiapp.composeapp.generated.resources.ic_info_filled
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.compose.resources.painterResource
import ru.lavafrai.maiapp.LibRes
import ru.lavafrai.maiapp.fragments.AnimatedIcon
import soup.compose.material.motion.MaterialMotion
import soup.compose.material.motion.animation.materialSharedAxisX
import soup.compose.material.motion.animation.rememberSlideDistance

val mainNavigationItems = listOf(
    MainNavigationItem(
        id = MainNavigationPageId.INFORMATION,
        // icon = { selected -> Icon(painterResource(if (selected) LibRes.image.info_filled else LibRes.image.info), "Info") },
        icon = { selected -> AnimatedIcon(
            iconPainter = painterResource(Res.drawable.ic_info),
            enabledIconPainter = painterResource(Res.drawable.ic_info_filled),
            contentDescription = "Info",
            enabled = selected,
        )},
        title = { Text("Info") },
    ),
    MainNavigationItem(
        id = MainNavigationPageId.WORKS,
        icon = { selected -> AnimatedIcon(
            iconPainter = painterResource(Res.drawable.ic_award),
            enabledIconPainter = painterResource(Res.drawable.ic_award_filled),
            contentDescription = "Works",
            enabled = selected,
        )},
        title = { Text("Works") },
    ),
    MainNavigationItem(
        id = MainNavigationPageId.HOME,
        icon = { selected -> AnimatedIcon(
            iconPainter = painterResource(Res.drawable.ic_home),
            enabledIconPainter = painterResource(Res.drawable.ic_home_filled),
            contentDescription = "Home",
            enabled = selected,
        )},
        title = { Text("Home") },
    ),
    MainNavigationItem(
        id = MainNavigationPageId.ACCOUNT,
        icon = { selected -> AnimatedIcon(
            iconPainter = painterResource(Res.drawable.ic_user),
            enabledIconPainter = painterResource(Res.drawable.ic_user_filled),
            contentDescription = "Account",
            enabled = selected,
        )},
        title = { Text("Account") },
    ),
    MainNavigationItem(
        id = MainNavigationPageId.SETTINGS,
        icon = { selected -> AnimatedIcon(
            iconPainter = painterResource(Res.drawable.ic_settings),
            enabledIconPainter = painterResource(Res.drawable.ic_settings_filled),
            contentDescription = "Settings",
            enabled = selected,
        )},
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
                        icon = { item.icon(selectedItem.id == item.id) },
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
                    icon = { item.icon(selectedItem.id == item.id) },
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
