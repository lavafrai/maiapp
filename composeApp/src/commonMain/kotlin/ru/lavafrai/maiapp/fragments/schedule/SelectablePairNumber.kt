package ru.lavafrai.maiapp.fragments.schedule

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


@Composable
fun SelectablePairNumber(
    modifier: Modifier = Modifier,
    text: String,
    size: Dp = 36.dp,
    borderWidth: Dp = 3.dp,
    selected: Boolean = false,
    onSelectionChange: (Boolean) -> Unit = {},
) {
    val textColor = animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimary
        else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
    )
    val backgroundColor = animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.surfaceContainer,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
    )
    val borderColor = animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
    )

    PairNumber(
        text = text,
        modifier = modifier,
        size = size,
        borderWidth = borderWidth,
        onClick = { onSelectionChange(!selected) },
        background = backgroundColor.value,
        borderColor = borderColor.value,
        color = textColor.value
    )

    /*if (selected) PairNumber(
        text = text,
        modifier = modifier,
        size = size,
        borderWidth = borderWidth,
        onClick = { onSelectionChange(!selected) },
        background = MaterialTheme.colorScheme.primary,
    ) else PairNumber(
        text = text,
        modifier = modifier,
        size = size,
        borderWidth = borderWidth,
        onClick = { onSelectionChange(!selected) },
        background = MaterialTheme.colorScheme.surfaceContainer,
        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
    )*/
}