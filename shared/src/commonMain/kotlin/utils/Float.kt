package ru.lavafrai.maiapp.utils

import kotlin.math.pow
import kotlin.math.roundToInt

fun Double.toString(numOfDec: Int): String {
    val integerPart = this.toInt()

    if (numOfDec > 0) {
        val num = this.toString()
        val res = "${num.split(".")[0]}.${num.split(".")[1].substring(0, 2)}"

        return res
    } else {
        return integerPart.toString()
    }
}

fun Float.toHex(length: Int): String {
    return toInt().toString(16).padStart(length, '0')
}