@file:OptIn(ExperimentalFoundationApi::class)

package ru.lavafrai.maiapp.fragments

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    cardBackground: Color = MaterialTheme.colorScheme.surfaceVariant,
    cardContent: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    shape: CornerBasedShape = AppCardShapes.default(),
    content: @Composable ColumnScope.() -> Unit
) {
    CompositionLocalProvider(LocalContentColor provides cardContent) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .clip(shape)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick,
                )
                .background(cardBackground)
                .padding(8.dp),
            content = content,
            verticalArrangement = verticalArrangement,
            horizontalAlignment = horizontalAlignment,
        )
    }
}


typealias AppCardShape = CornerBasedShape

object AppCardShapes {
    @Composable
    fun default() = MaterialTheme.shapes.medium

    @Composable
    fun first(): RoundedCornerShape {
        val small = MaterialTheme.shapes.extraSmall
        val large = MaterialTheme.shapes.extraLarge

        return RoundedCornerShape(
            topStart = large.topStart,
            topEnd = large.topEnd,
            bottomStart = small.bottomStart,
            bottomEnd = small.bottomEnd
        )
    }

    @Composable
    fun middle(): RoundedCornerShape {
        val small = MaterialTheme.shapes.extraSmall
        val large = MaterialTheme.shapes.extraLarge

        return RoundedCornerShape(
            topStart = small.topStart,
            topEnd = small.topEnd,
            bottomStart = small.bottomStart,
            bottomEnd = small.bottomEnd
        )
    }

    @Composable
    fun last(): RoundedCornerShape {
        val small = MaterialTheme.shapes.extraSmall
        val large = MaterialTheme.shapes.large

        return RoundedCornerShape(
            topStart = small.topStart,
            topEnd = small.topEnd,
            bottomStart = large.bottomStart,
            bottomEnd = large.bottomEnd
        )
    }

    @Composable
    fun firstLast(): RoundedCornerShape {
        val small = MaterialTheme.shapes.extraSmall
        val large = MaterialTheme.shapes.large
        val extraLarge = MaterialTheme.shapes.extraLarge

        return RoundedCornerShape(
            topStart = extraLarge.topStart,
            topEnd = extraLarge.topEnd,
            bottomStart = large.bottomStart,
            bottomEnd = large.bottomEnd
        )
    }
}