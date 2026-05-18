package ru.lavafrai.maiapp.fragments.ripleLoadingIndicator

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.time.Duration.Companion.seconds

@Composable
fun RippleLoadingIndicator(
    indicatorSize: Dp = RippleLoadingIndicatorDefaults.size,
    color: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    rippleAnimationSpec: InfiniteRepeatableSpec<Float> = RippleLoadingIndicatorDefaults.animationSpec,
) {
    val transition = rememberInfiniteTransition()
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = rippleAnimationSpec,
    )

    Canvas(modifier = Modifier.size(indicatorSize)) {
        val secondRippleOffset = 0.4f
        val firstRippleProgress = progress
        val secondRippleProgress = (if (progress < 0.3f) 0f else (progress - secondRippleOffset) / (1 - secondRippleOffset)).coerceAtMost(1f)


        drawCircle(color)
        drawCircle(
            color = backgroundColor,
            radius = size.width / 2 * firstRippleProgress,
        )
        drawCircle(
            color = color,
            radius = size.width / 2 * secondRippleProgress,
        )
    }
}

object RippleLoadingIndicatorDefaults {
    val size = 48.dp
    val duration = 1.seconds
    val animationSpec = infiniteRepeatable<Float>(
        animation =
            tween(
                durationMillis = duration.inWholeMilliseconds.toInt(),
                easing = EaseInOutSine
            )
    )
}