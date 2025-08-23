package ru.lavafrai.maiapp.utils

import co.touchlab.kermit.Logger
import kotlinx.datetime.Clock
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

object SeasonedThemeHelper {
    fun isNewYearThemeActive(): Boolean {
        val currentMonth = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .month
        Logger.d("Current month: $currentMonth")
        return currentMonth in setOf(Month.DECEMBER, Month.JANUARY)
    }
}