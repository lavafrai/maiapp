package ru.lavafrai.maiapp.ru.lavafrai.maiapp.widget

import android.content.Context

/**
 * Какое расписание показывает конкретный экземпляр виджета и когда его данные проверялись.
 *
 * Хранится отдельно от настроек приложения, по id экземпляра виджета: благодаря этому на рабочем
 * столе могут висеть несколько виджетов с разными расписаниями, а у остальных остаётся выбранное
 * в приложении.
 */
object WidgetScheduleState {
    private const val PREFERENCES_NAME = "widget_schedules"
    private const val SERVER_UPDATED_SUFFIX = ":server_updated"
    private const val SERVER_CHECK_FAILED_SUFFIX = ":server_failed"

    private fun preferences(context: Context) =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    /** Название расписания, выбранное для этого экземпляра, или null, если выбор ещё не сделан */
    fun scheduleName(context: Context, appWidgetId: Int): String? {
        return preferences(context).getString(appWidgetId.toString(), null)
    }

    fun setScheduleName(context: Context, appWidgetId: Int, scheduleName: String) {
        preferences(context).edit().putString(appWidgetId.toString(), scheduleName).apply()
    }

    /** Когда виджет последний раз сам получил расписание с сервера, или null, если ещё не получал */
    fun serverUpdatedAt(context: Context, appWidgetId: Int): Long? {
        return preferences(context)
            .getLong(appWidgetId.toString() + SERVER_UPDATED_SUFFIX, 0L)
            .takeIf { it > 0L }
    }

    fun setServerUpdatedAt(context: Context, appWidgetId: Int, epochMillis: Long) {
        preferences(context)
            .edit()
            .putLong(appWidgetId.toString() + SERVER_UPDATED_SUFFIX, epochMillis)
            .apply()
    }

    /** Была ли последняя проверка сервера неудачной */
    fun serverCheckFailed(context: Context, appWidgetId: Int): Boolean {
        return preferences(context)
            .getBoolean(appWidgetId.toString() + SERVER_CHECK_FAILED_SUFFIX, false)
    }

    fun setServerCheckFailed(context: Context, appWidgetId: Int, failed: Boolean) {
        preferences(context)
            .edit()
            .putBoolean(appWidgetId.toString() + SERVER_CHECK_FAILED_SUFFIX, failed)
            .apply()
    }

    /** Вызывается при удалении виджета с рабочего стола */
    fun remove(context: Context, appWidgetIds: IntArray) {
        val editor = preferences(context).edit()
        appWidgetIds.forEach {
            editor.remove(it.toString())
            editor.remove(it.toString() + SERVER_UPDATED_SUFFIX)
            editor.remove(it.toString() + SERVER_CHECK_FAILED_SUFFIX)
        }
        editor.apply()
    }
}
