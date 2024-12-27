package ru.lavafrai.maiapp.ru.lavafrai.maiapp.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.*
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import co.touchlab.kermit.Logger
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import ru.lavafrai.maiapp.ru.lavafrai.maiapp.widget.text.GlanceTitle
import ru.lavafrai.maiapp.ru.lavafrai.maiapp.widget.text.LocalGlanceTextStyle
import ru.lavafrai.maiapp.R
import ru.lavafrai.maiapp.data.repositories.ScheduleRepository
import ru.lavafrai.maiapp.data.settings.ApplicationSettings
import ru.lavafrai.maiapp.models.schedule.Schedule
import ru.lavafrai.maiapp.models.time.now
import ru.lavafrai.maiapp.ru.lavafrai.maiapp.widget.text.GlanceText
import ru.lavafrai.maiapp.localizers.*

class ScheduleWidget: GlanceAppWidget() {
    private val widgetBackground = Color(0xE0000000)

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val settings = ApplicationSettings.getCurrent()
        val group = settings.selectedSchedule
        Logger.i("Loading schedule for widget $id")

        val schedule = if (group != null) ScheduleRepository().getScheduleFromCacheOrNull(group) else null

        provideContent {
            Column(modifier = GlanceModifier
                .fillMaxSize()
                .cornerRadius(16.dp)
                .appWidgetBackground()
                .background(widgetBackground),
            ) {
                CompositionLocalProvider(LocalGlanceTextStyle provides TextStyle(color = ColorProvider(Color.White))) {
                    ScheduleWidgetContent(group, schedule)
                }
            }
        }
    }
}

@Composable
fun ScheduleWidgetContent(
    group: String?,
    schedule: Schedule?,
) {
    ScheduleWidgetHeader()
    if (schedule != null) ScheduleWidgetSchedule(schedule)
    else if (group != null) ScheduleWidgetNotLoaded()
    else ScheduleWidgetNotLoggedIn()
}

@Composable
fun ScheduleWidgetSchedule(
    schedule: Schedule,
) {
    val today = LocalDate.now()
    val filteredDays = schedule.days
        .filter { it.date >= today }
        .filter { it.lessons.isNotEmpty() }
        .filter { it.date.plus(DatePeriod(days = 7)) < today }


    LazyColumn () {
        items(filteredDays.size) { dayIndex ->
            val day = filteredDays[dayIndex]
            GlanceText(day.date.toString())
        }
    }
}

@Composable
fun ScheduleWidgetNotLoaded() {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        GlanceTitle("Расписание не загружено")
    }
}

@Composable
fun ScheduleWidgetNotLoggedIn() {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        GlanceTitle("Необходимо войти в приложение")
    }
}

@Composable
fun ScheduleWidgetHeader() {
    val today = LocalDate.now()
    Column {
        Box(modifier = GlanceModifier.padding(8.dp), contentAlignment = Alignment.Center) {
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.Start,
            ) {
                GlanceTitle("27 ${today.month.localizedGenitive()} ${today.dayOfWeek.localized()}")
            }

            Row(
                modifier = GlanceModifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.End,
            ) {
                Image(
                    ImageProvider(R.drawable.ic_refresh),
                    null,
                    modifier = GlanceModifier
                        .size(18.dp)
                        .clickable {  },
                )
            }
        }

        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .height(1.dp)
                .background(ColorProvider(Color(0x4DFFFFFF))),
        ) { }
    }
}