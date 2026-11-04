package ru.lavafrai.maiapp.ru.lavafrai.maiapp.widget

import android.content.Context

/**
 * Какое расписание показывает конкретный экземпляр виджета.
 *
 * Хранится отдельно от настроек приложения, по id экземпляра виджета: благодаря этому на рабочем
 * столе могут висеть несколько виджетов с разными расписаниями, а у остальных остаётся выбранное
 * в приложении.
 */
object WidgetScheduleState {
    private const val PREFERENCES_NAME = "widget_schedules"

    private fun preferences(context: Context) =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    /** Название расписания, выбранное для этого экземпляра, или null, если выбор ещё не сделан */
    fun scheduleName(context: Context, appWidgetId: Int): String? {
        return preferences(context).getString(appWidgetId.toString(), null)
    }

    fun setScheduleName(context: Context, appWidgetId: Int, scheduleName: String) {
        preferences(context).edit().putString(appWidgetId.toString(), scheduleName).apply()
    }

    /** Вызывается при удалении виджета с рабочего стола */
    fun remove(context: Context, appWidgetIds: IntArray) {
        val editor = preferences(context).edit()
        appWidgetIds.forEach { editor.remove(it.toString()) }
        editor.apply()
    }
}
