package ru.lavafrai.maiapp.ru.lavafrai.maiapp.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.AppWidgetId
import androidx.glance.appwidget.action.ActionCallback

/**
 * Выбор расписания для одного экземпляра виджета.
 *
 * Только переключает и перерисовывает: данные берутся из кэша приложения, поэтому выбор
 * срабатывает сразу. За сервер отвечает кнопка обновления и прогрев кэша в приложении.
 */
class ChooseWidgetScheduleAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val scheduleName = parameters[ScheduleNameKey] ?: return
        val appWidgetId = (glanceId as? AppWidgetId)?.appWidgetId ?: return

        WidgetScheduleState.setScheduleName(context, appWidgetId, scheduleName)
        ScheduleWidget().update(context, glanceId)
    }

    companion object {
        val ScheduleNameKey = ActionParameters.Key<String>("scheduleName")
    }
}
