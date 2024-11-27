package ru.lavafrai.maiapp.fragments.animations

import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha

@Composable
fun Modifier.pulsatingTransparency(
    initialValue: Float = 0.3f,
    targetValue: Float = 0.8f,
): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "pulsating-transparency")
    val alpha by infiniteTransition.animateFloat(
        initialValue = initialValue,
        targetValue = targetValue,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
    )

    return this.alpha(alpha)
}