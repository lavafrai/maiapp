package ru.lavafrai.maiapp.ru.lavafrai.maiapp.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class ScheduleWidgetReceiver: GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ScheduleWidget()

    // Виджет удалили с рабочего стола — забываем и выбранное для него расписание
    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        WidgetScheduleState.remove(context, appWidgetIds)
    }
}
