package ru.lavafrai.maiapp.fragments.animations

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State

@Composable
fun animateShapeAsState(
    targetValue: androidx.compose.foundation.shape.CornerBasedShape,
    animationSpec: androidx.compose.animation.core.AnimationSpec<androidx.compose.foundation.shape.CornerBasedShape> = androidx.compose.animation.core.spring(),
    finishedListener: ((androidx.compose.foundation.shape.CornerBasedShape) -> Unit)? = null
): State<CornerBasedShape> {
    TODO()
}