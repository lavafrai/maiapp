package ru.lavafrai.maiapp.ru.lavafrai.maiapp.widget.text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.glance.text.FontFamily
import androidx.glance.text.Text
import androidx.glance.text.TextStyle

val LocalGlanceTextStyle = compositionLocalOf<TextStyle> { error("No LocalTextStyle provided") }

@Composable
fun GlanceText(
    text: String,
    fontSize: TextUnit? = null,
    maxLines: Int = Int.MAX_VALUE,
    fontFamily: FontFamily = FontFamily.SansSerif,
) {
    val textStyle = LocalGlanceTextStyle.current.copy(
        fontSize = fontSize,
        fontFamily = fontFamily,
    )
    Text(
        text = text,
        style = textStyle,
        maxLines = maxLines,
    )
}

@Composable
fun GlanceTitle(
    text: String,
) {
    val textStyle = LocalGlanceTextStyle.current.copy(
        fontSize = 18.sp,
        fontWeight = androidx.glance.text.FontWeight.Bold,
    )
    Text(
        text = text,
        style = textStyle,
    )
}

