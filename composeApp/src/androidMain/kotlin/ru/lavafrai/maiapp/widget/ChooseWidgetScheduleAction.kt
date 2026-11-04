package ru.lavafrai.maiapp.ru.lavafrai.maiapp.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.AppWidgetId
import androidx.glance.appwidget.action.ActionCallback
import co.touchlab.kermit.Logger

/**
 * Выбор расписания для одного экземпляра виджета: запоминает его и перерисовывает только этот виджет.
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
        Logger.i("Widget $appWidgetId shows schedule $scheduleName")
    }

    companion object {
        val ScheduleNameKey = ActionParameters.Key<String>("scheduleName")
    }
}
