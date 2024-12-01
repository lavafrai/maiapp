package ru.lavafrai.maiapp.navigation.rootPages.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import compose.icons.FeatherIcons
import compose.icons.feathericons.DownloadCloud
import ru.lavafrai.maiapp.fragments.animations.pulsatingTransparency
import ru.lavafrai.maiapp.utils.asDp

@Composable
fun MainPageTitle(
    modifier: Modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
    titleText: @Composable () -> Unit,
    subtitleText: @Composable () -> Unit = { Text("") },
    leftButton: @Composable () -> Unit = {},
    rightButton: @Composable () -> Unit = {},
    subtitleIcon: @Composable (Dp) -> Unit = {},
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
            leftButton()
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
                    subtitleIcon(MaterialTheme.typography.titleMedium.fontSize.asDp.div(1.2f))
                }
            }
            // Right button
            rightButton()
        }
        Box(
            Modifier
                .padding(top = 4.dp)
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
        ) {}
    }
}