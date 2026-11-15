package ru.lavafrai.maiapp.ru.lavafrai.maiapp.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.AppWidgetId
import androidx.glance.appwidget.action.ActionCallback
import ru.lavafrai.maiapp.data.repositories.ScheduleRepository
import ru.lavafrai.maiapp.data.settings.ApplicationSettings
import ru.lavafrai.maiapp.models.schedule.BaseScheduleId

class RefreshScheduleWidgetAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val appWidgetId = (glanceId as? AppWidgetId)?.appWidgetId
        val scheduleName = appWidgetId?.let { WidgetScheduleState.scheduleName(context, it) }
            ?: ApplicationSettings.getCurrent().selectedSchedule?.scheduleId

        if (scheduleName != null) {
            val loaded = loadScheduleFromServer(scheduleName)
            ScheduleWidgetCache.clear()

            if (appWidgetId != null) {
                WidgetScheduleState.setServerCheckFailed(context, appWidgetId, !loaded)
                if (loaded) {
                    WidgetScheduleState.setServerUpdatedAt(context, appWidgetId, System.currentTimeMillis())
                }
            }
        }

        // Одна отрисовка после загрузки: промежуточные состояния лаунчеры применяют ненадёжно
        ScheduleWidget().update(context, glanceId)
    }
}

/** Загружает расписание с сервера; при успехе его данные попадают в кэш приложения */
internal suspend fun loadScheduleFromServer(scheduleName: String): Boolean {
    return runCatching { ScheduleRepository().getSchedule(BaseScheduleId(scheduleName)) }.isSuccess
}
