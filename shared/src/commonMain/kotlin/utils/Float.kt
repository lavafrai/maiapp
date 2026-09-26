package ru.lavafrai.maiapp.utils

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToLong

/** Formats with exactly [numOfDec] digits after the point, rounding the rest (4.5 -> "4.50") */
fun Double.toString(numOfDec: Int): String {
    if (numOfDec <= 0) return toInt().toString()

    val factor = 10.0.pow(numOfDec).toLong()
    val rounded = (this * factor).roundToLong()
    val sign = if (rounded < 0) "-" else ""
    val integerPart = abs(rounded) / factor
    val fractionPart = (abs(rounded) % factor).toString().padStart(numOfDec, '0')
    return "$sign$integerPart.$fractionPart"
}

fun Float.toHex(length: Int): String {
    return toInt().toString(16).padStart(length, '0')
}
