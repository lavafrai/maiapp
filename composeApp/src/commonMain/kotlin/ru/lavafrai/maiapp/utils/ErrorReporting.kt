package ru.lavafrai.maiapp.utils

import co.touchlab.kermit.Logger
import ru.lavafrai.maiapp.platform.getPlatform

/**
 * Logs a handled error and sends it to AppMetrica. Never throws.
 *
 * `JSON input` lines are cut out: kotlinx.serialization puts a piece of the response there,
 * and responses of my.mai.ru contain personal data and tokens.
 */
fun reportError(context: String, error: Throwable) {
    Logger.e(context, error)
    try {
        val details = error.stackTraceToString()
            .lines()
            .filterNot { it.trimStart().startsWith("JSON input") }
            .joinToString("\n")
        getPlatform().reportError(context, error, details)
    } catch (e: Exception) {
        Logger.e("Failed to report error", e)
    }
}
