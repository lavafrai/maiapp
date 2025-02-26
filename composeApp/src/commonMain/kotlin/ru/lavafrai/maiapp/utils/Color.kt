package ru.lavafrai.maiapp.utils

import androidx.compose.ui.graphics.Color

fun Color.toHex(): String {
    return "#${(red * 255).toHex(2)}${(green * 255).toHex(2)}${(blue * 255).toHex(2)}"
}

