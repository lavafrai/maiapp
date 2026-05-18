package ru.lavafrai.maiapp.fragments.account

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ru.lavafrai.maiapp.utils.toString

@Composable
fun MarkView(mark: Double, modifier: Modifier = Modifier) = MarkView(mark.toString(2), color = detectMarkColor(mark), modifier = modifier)

@Composable
fun MarkView(mark: Int, modifier: Modifier = Modifier) = MarkView(mark.toString(), color = detectMarkColor(mark.toDouble()), modifier = modifier)

@Composable
fun MarkView(mark: String, color: Color = detectMarkColor(mark), modifier: Modifier = Modifier) {
    if (mark.isNotBlank()) Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.extraSmall)
            .background(color)
            .padding(vertical = 4.dp, horizontal = 6.dp)
    ) {
        Text(mark, color = MarkTextColor)
    }
    else Box(
        modifier = modifier
            .border(
                1.dp,
                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                MaterialTheme.shapes.extraSmall
            )
            .padding(vertical = 4.dp, horizontal = 6.dp)
    ) {
        Text(text = "  ")
    }
}

fun detectMarkColor(mark: Double): Color {
    if (mark < 2.5) return MarkRedColor
    if (mark < 3.5) return MarkOrangeColor
    if (mark < 4.5) return MarkYellowColor
    return  MarkGreenColor
}

fun detectMarkColor(mark: String) = when (mark) {
    "Зч" -> MarkGreenColor
    "5" -> MarkGreenColor
    "4" -> MarkYellowColor
    "3" -> MarkOrangeColor
    "2" -> MarkRedColor
    "Нзч" -> MarkRedColor
    "Ня" -> MarkRedColor

    else -> MarkRedColor
}

val MarkGreenColor = Color(0xFF7fce51)
val MarkYellowColor = Color(0xFFc0d242)
val MarkOrangeColor = Color(0xFFf89f3c)
val MarkRedColor = Color(0xFFe4726a)
val MarkTextColor = Color(0xFFffffff)
