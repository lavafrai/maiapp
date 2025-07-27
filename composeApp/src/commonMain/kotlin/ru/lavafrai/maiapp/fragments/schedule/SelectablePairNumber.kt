package ru.lavafrai.maiapp.fragments.schedule

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
    if (selected) PairNumber(
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
    )
}