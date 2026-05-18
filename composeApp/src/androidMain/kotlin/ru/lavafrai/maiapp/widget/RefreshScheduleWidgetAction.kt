package ru.lavafrai.maiapp.ru.lavafrai.maiapp.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.updateAll
import co.touchlab.kermit.Logger

class RefreshScheduleWidgetAction: ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        ScheduleWidget().updateAll(context)
        Logger.i("Refreshed schedule widget $glanceId")
    }
}