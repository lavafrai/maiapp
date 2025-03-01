package ru.lavafrai.maiapp.rootPages.map

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import compose.icons.FeatherIcons
import compose.icons.feathericons.ArrowLeft
import compose.icons.feathericons.RotateCw
import ru.lavafrai.maiapp.rootPages.main.MainPageTitle


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MapPage(
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onNavigateBack: () -> Unit,
    url: String,
    title: String,
) = Column(modifier = Modifier.fillMaxSize()) {
    val loadHash = remember { mutableStateOf(0) }

    MainPageTitle(
        titleText = { Text(title) },
        leftButton = {
            IconButton(onClick = onNavigateBack) {
                Icon(FeatherIcons.ArrowLeft, contentDescription = "back")
            }
        },
        rightButton = {

        }
    )


}