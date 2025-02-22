package ru.lavafrai.maiapp.utils

import kotlin.math.pow
import kotlin.math.roundToInt

fun Double.toString(numOfDec: Int): String {
    val integerPart = this.toInt()

    if (numOfDec > 0) {
        val decimalPart = this - integerPart
        val decimalAsIntValue = (decimalPart * 10f.pow(numOfDec)).roundToInt()
        val formattedDecimalPart = decimalAsIntValue.toDouble() / 10f.pow(numOfDec)
        val formattedValue = integerPart + formattedDecimalPart

        return "$formattedValue"
    } else {
        return integerPart.toString()
    }
}