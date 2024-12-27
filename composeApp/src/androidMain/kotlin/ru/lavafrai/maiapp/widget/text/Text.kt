package ru.lavafrai.maiapp.ru.lavafrai.maiapp.widget.text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.sp
import androidx.glance.text.Text
import androidx.glance.text.TextStyle

val LocalGlanceTextStyle = compositionLocalOf<TextStyle> { error("No LocalTextStyle provided") }

@Composable
fun GlanceText(
    text: String,
) {
    val textStyle = LocalGlanceTextStyle.current
    Text(
        text = text,
        style = textStyle,
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

