package ru.lavafrai.maiapp.utils

import kotlin.math.roundToInt


val siPrefixes = listOf("", "k", "M", "G")

fun Long.formatBinarySize(): String {
    var value = this.toDouble()
    var usedPrefix = 0

    while (value > 900 && usedPrefix < siPrefixes.size - 1) {
        value /= 1024
        usedPrefix += 1
    }

    return "${value.roundToInt()} ${siPrefixes[usedPrefix]}B"
}