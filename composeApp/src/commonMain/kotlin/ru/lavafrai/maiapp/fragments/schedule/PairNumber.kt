package ru.lavafrai.maiapp.fragments.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun PairNumber(
    modifier: Modifier = Modifier,
    text: String = "1",
    background: Color = MaterialTheme.colorScheme.primary,
    color: Color = MaterialTheme.colorScheme.onPrimary,
    size: Dp = 36.dp,
    borderWidth: Dp = 1.dp,
    borderColor: Color = Color.Transparent,
    bold: Boolean = false
) {
    Box (
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(borderColor),
    ) {
        Surface(
            modifier = Modifier
                .clip(CircleShape)
                .size(size.minus(borderWidth))
                .align(Alignment.Center),
            color = background
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text,
                    color = color,
                    textAlign = TextAlign.Center,
                    fontSize = 18.sp,
                    fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium,
                )
            }
        }
    }
}