package ru.lavafrai.maiapp.utils

fun String.capitalizeWords(): String =
    split(" ").joinToString(" ") { it.lowercase()
        .replaceFirstChar { letter -> if (letter.isLowerCase()) letter.titlecase() else it } }
