package ru.lavafrai.maiapp.navigation.rootPages.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import compose.icons.FeatherIcons
import compose.icons.feathericons.DownloadCloud
import compose.icons.feathericons.Loader
import ru.lavafrai.maiapp.fragments.animations.pulsatingTransparency
import ru.lavafrai.maiapp.utils.asDp

@Composable
fun MainPageTitle(
    modifier: Modifier = Modifier,
    titleText: @Composable () -> Unit,
    subtitleText: @Composable () -> Unit,
    rightButton: @Composable () -> Unit = {},
) {
    Column(modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp)
        .padding(top = 4.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                // Title
                CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.titleLarge) {
                    titleText()
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.titleMedium) {
                        subtitleText()
                    }
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        FeatherIcons.DownloadCloud,
                        "loading",
                        modifier = modifier
                            .size(MaterialTheme.typography.titleMedium.fontSize.asDp.div(1.2f))
                            .pulsatingTransparency(),
                    )
                }
            }
            // Right button
            rightButton()
        }
    }
}