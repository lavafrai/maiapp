package ru.lavafrai.maiapp.fragments

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


@Composable
fun CrossedChip(
    onClick: () -> Unit = {},
    content: @Composable () -> Unit,
) {
    Row {
        Row(
            modifier = Modifier
                .clip(MaterialTheme.shapes.small)
                .clickable(onClick = onClick)
                .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), MaterialTheme.shapes.small)
                .padding(horizontal = 16.dp)
                .height(32.dp)
                .wrapContentWidth()
                .widthIn(min = 38.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box {
                val crossColor = MaterialTheme.colorScheme.onSurfaceVariant
                Canvas(modifier = Modifier.matchParentSize().height(32.dp)) {
                    drawLine(
                        color = crossColor,
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height),
                        strokeWidth = 1.dp.toPx(),
                    )
                }

                content()
            }
        }
    }
}