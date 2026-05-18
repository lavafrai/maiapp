package ru.lavafrai.maiapp.fragments.slideButton

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.*
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun SlideButton(
    modifier: Modifier = Modifier,
    size: SwipeableButtonSize = SwipeableButtonSize.Large,
    shape: Shape = CircleShape,
) {
    val progressState = remember { mutableFloatStateOf(0f) }
    val endOfTrackState = remember { mutableIntStateOf(0) }
    var offset by remember { mutableStateOf(0f) }
    var log by remember { mutableStateOf("log") }

    Layout(
        content = {
            Box(
                modifier = Modifier
                    .layoutId(SlideButtonLayout.ThumbLayout)
                    .clip(shape)
                    .background(Color.Green),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Thumb")
            }
        },
        modifier = Modifier
            .background(Color.Red),
        measurePolicy = slideButtonMeasurePolicy(
            size = size,
            progressState = progressState,
            endOfTrackState = endOfTrackState,
        )
    )
}

private fun slideButtonMeasurePolicy(
    size: SwipeableButtonSize,
    endOfTrackState: MutableIntState,
    progressState: MutableFloatState,
): MeasureScope.(measurables: List<Measurable>, constraints: Constraints) -> MeasureResult {
    return { measurables, constraints ->
        val thumbPlaceable = measurables.first { it.layoutId == SlideButtonLayout.ThumbLayout }.measure(
            constraints.copy(
                minHeight = constraints.minHeight.coerceAtLeast(size.minHeight.roundToPx()),
                minWidth = constraints.minWidth.coerceAtLeast(size.minWidth.roundToPx()),
            )
        )
        val height = thumbPlaceable.height
        val thumbWidth = thumbPlaceable.width
        val endOfTrackWidth = constraints.maxWidth - thumbWidth

        layout(constraints.maxWidth, height) {
            thumbPlaceable.placeRelative(x = 100, y = 0)
        }
    }
}

enum class SwipeableButtonSize(
    val minWidth: Dp,
    val minHeight: Dp,
) {
    Small(minWidth = 40.dp, minHeight = 40.dp),
    Medium(minWidth = 48.dp, minHeight = 48.dp),
    Large(minWidth = 56.dp, minHeight = 56.dp)
}

private enum class SlideButtonLayout {
    ThumbLayout, ProcessLayout, EndLayout, CenterLayout
}