package ru.lavafrai.maiapp.fragments.fixes

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.SplitButtonDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.unit.*
import androidx.compose.ui.util.fastFirst
import androidx.compose.ui.util.fastMaxOfOrNull

@ExperimentalMaterial3ExpressiveApi
@Composable
fun SplitButtonLayoutFixed(
    leadingButton: @Composable () -> Unit,
    trailingButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    spacing: Dp = SplitButtonDefaults.Spacing,
) {
    Layout(
        {
            // Override min component size enforcement to avoid create extra padding internally
            // Enforce it on the parent instead
            CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                Box(
                    modifier = Modifier.layoutId(LeadingButtonLayoutId),
                    contentAlignment = Alignment.Center,
                    content = { leadingButton() },
                )
                Box(
                    modifier = Modifier.layoutId(TrailingButtonLayoutId),
                    contentAlignment = Alignment.Center,
                    content = { trailingButton() },
                )
            }
        },
        modifier.minimumInteractiveComponentSize(),
        measurePolicy = { measurables, constraints ->
            val spacingPx = spacing.roundToPx()
            val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)

            // Measure trailing button first at its intrinsic size
            val trailingButtonPlaceable =
                measurables
                    .fastFirst { it.layoutId == TrailingButtonLayoutId }
                    .measure(looseConstraints)

            // Remaining width for leading button
            val remainingWidth = if (constraints.maxWidth != Constraints.Infinity) {
                (constraints.maxWidth - trailingButtonPlaceable.width - spacingPx).coerceAtLeast(0)
            } else {
                // Unbounded width – just measure intrinsically
                Constraints.Infinity
            }

            val leadingConstraints = if (remainingWidth != Constraints.Infinity) {
                // Force leading button to occupy all remaining width
                constraints.copy(
                    minWidth = remainingWidth,
                    maxWidth = remainingWidth,
                    minHeight = trailingButtonPlaceable.height,
                    maxHeight = trailingButtonPlaceable.height,
                )
            } else {
                // Unbounded width scenario – keep original behaviour
                looseConstraints.copy(
                    minHeight = trailingButtonPlaceable.height,
                    maxHeight = trailingButtonPlaceable.height,
                )
            }

            val leadingButtonPlaceable =
                measurables
                    .fastFirst { it.layoutId == LeadingButtonLayoutId }
                    .measure(leadingConstraints)

            val placeables = listOf(leadingButtonPlaceable, trailingButtonPlaceable)

            val contentWidth = leadingButtonPlaceable.width + trailingButtonPlaceable.width + spacingPx
            val contentHeight = placeables.fastMaxOfOrNull { it.height } ?: 0

            val width = constraints.constrainWidth(contentWidth)
            val height = constraints.constrainHeight(contentHeight)

            layout(width, height) {
                leadingButtonPlaceable.placeRelative(0, 0)
                trailingButtonPlaceable.placeRelative(
                    x = leadingButtonPlaceable.width + spacingPx,
                    y = 0,
                )
            }
        },
    )
}

private const val LeadingButtonLayoutId = "LeadingButton"
private const val TrailingButtonLayoutId = "TrailingButton"
