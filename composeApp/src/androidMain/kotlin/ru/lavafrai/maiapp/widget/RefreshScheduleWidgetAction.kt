package ru.lavafrai.maiapp.ru.lavafrai.maiapp.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import co.touchlab.kermit.Logger

class RefreshScheduleWidgetAction: ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        // Обновляем только этот виджет: у каждого экземпляра может быть своё расписание
        ScheduleWidget().update(context, glanceId)
        Logger.i("Refreshed schedule widget $glanceId")
    }
}
