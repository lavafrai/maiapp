package ru.lavafrai.maiapp.fragments.schedule

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import ru.lavafrai.maiapp.data.settings.ScheduleLabel

/** Сколько символов можно ввести в подпись: иначе в список расписаний можно вписать «войну и мир» */
const val MaxScheduleLabelLength = 40

/** 24 цвета для подписей расписаний: различимы в светлой и тёмной теме, в палитре идут по 6 в ряд */
val ScheduleLabelColors = listOf(
    "#EF5350", "#EC407A", "#AB47BC", "#7E57C2", "#5C6BC0", "#42A5F5",
    "#29B6F6", "#26C6DA", "#26A69A", "#66BB6A", "#9CCC65", "#D4E157",
    "#FFEE58", "#FFCA28", "#FFA726", "#FF7043", "#8D6E63", "#78909C",
    "#BA68C8", "#9575CD", "#7986CB", "#64B5F6", "#4FC3F7", "#4DD0E1",
)

/** Цвет подписи из настроек; null, если цвет не задан или записан неверно */
fun scheduleLabelColor(color: String?): Color? {
    val hex = color?.removePrefix("#") ?: return null
    if (hex.length != 6) return null
    val value = hex.toLongOrNull(16) ?: return null
    return Color(0xFF000000L or value)
}

/**
 * Название расписания вместе с подписью: подпись идёт тем же текстом через пробел,
 * чтобы строка переносилась естественно и одинаково выглядела в заголовке и в списках
 */
@Composable
fun ScheduleName(
    name: String,
    label: ScheduleLabel?,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    val labelColor = scheduleLabelColor(label?.color) ?: MaterialTheme.colorScheme.primary

    Text(
        text = buildAnnotatedString {
            append(name)
            if (label != null) {
                append(" ")
                withStyle(SpanStyle(color = labelColor)) { append(label.text) }
            }
        },
        modifier = modifier,
        style = style,
        maxLines = maxLines,
        overflow = overflow,
    )
}
