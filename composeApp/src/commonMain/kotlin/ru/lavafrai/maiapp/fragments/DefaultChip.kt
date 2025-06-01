package ru.lavafrai.maiapp.fragments

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


@Composable
fun DefaultChip(
    onClick: () -> Unit = {},
    label: @Composable () -> Unit,
    horizontalPadding: Dp = 16.dp,
    height: Dp = 32.dp,
) {
    CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.labelMedium) {
        Row {
            Row(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .clickable(onClick = onClick)
                    .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), MaterialTheme.shapes.small)
                    .padding(horizontal = horizontalPadding)
                    .height(height)
                    .wrapContentWidth()
                    .widthIn(min = 38.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box {
                    label()
                }
            }
        }
    }
}