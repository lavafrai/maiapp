package ru.lavafrai.maiapp.ru.lavafrai.maiapp.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontFamily
import androidx.glance.text.TextDefaults
import androidx.glance.unit.ColorProvider
import co.touchlab.kermit.Logger
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import ru.lavafrai.maiapp.AppActivity
import ru.lavafrai.maiapp.R
import ru.lavafrai.maiapp.data.repositories.ScheduleRepository
import ru.lavafrai.maiapp.data.settings.ApplicationSettings
import ru.lavafrai.maiapp.localizers.localizedGenitiveNonContext
import ru.lavafrai.maiapp.localizers.localizedNonContext
import ru.lavafrai.maiapp.localizers.localizedShortNonContext
import ru.lavafrai.maiapp.localizers.toApplication
import ru.lavafrai.maiapp.models.schedule.BaseScheduleId
import ru.lavafrai.maiapp.models.schedule.Lesson
import ru.lavafrai.maiapp.models.schedule.Schedule
import ru.lavafrai.maiapp.models.schedule.ScheduleDay
import ru.lavafrai.maiapp.models.time.now
import ru.lavafrai.maiapp.ru.lavafrai.maiapp.widget.text.GlanceText
import ru.lavafrai.maiapp.ru.lavafrai.maiapp.widget.text.GlanceTitle
import ru.lavafrai.maiapp.ru.lavafrai.maiapp.widget.text.LocalGlanceTextStyle
import ru.lavafrai.maiapp.theme.MaiColor

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
                .cornerRadius(26.dp)
                .appWidgetBackground()
                .background(widgetBackground),
            ) {
                CompositionLocalProvider(LocalGlanceTextStyle provides TextDefaults.defaultTextStyle.copy(
                    color = ColorProvider(Color.White),
                    fontSize = 14.sp,)
                ) {
                    ScheduleWidgetContent(group, schedule)
                }
            }
        }
    }
}

@Composable
fun ScheduleWidgetContent(
    group: BaseScheduleId?,
    schedule: Schedule?,
) {
    ScheduleWidgetHeader(modifier = GlanceModifier.clickable(actionStartActivity<AppActivity>()))
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
        .filter { it.date < today.plus(DatePeriod(days = 7)) }
        .filter { it.lessons.isNotEmpty() }

    LazyColumn(modifier = GlanceModifier.padding(horizontal = 8.dp)) {
        items(7) { dayIndex ->
            val day = today.plus(DatePeriod(days = dayIndex))
            val daySchedule =
                filteredDays.find { it.date == day } ?: ScheduleDay(day, day.dayOfWeek.toApplication(), emptyList())
            Column {
                Spacer(modifier = GlanceModifier.height(8.dp))
                ScheduleWidgetDay(daySchedule)
            }
        }
        item {
            Spacer(modifier = GlanceModifier.height(16.dp))
        }
    }
}

@Composable
fun ScheduleWidgetDay(
    day: ScheduleDay,
) {
    Column {
        ScheduleWidgetHeader(day)
        Spacer(modifier = GlanceModifier.height(6.dp))

        if (day.lessons.isNotEmpty()) day.lessons.forEach {
            ScheduleWidgetLesson(it)
            Spacer(modifier = GlanceModifier.height(4.dp))
        } else {
            Row {
                Spacer(modifier = GlanceModifier.width(8.dp))
                GlanceText("В этот день нет занятий")
            }
        }
    }
}

@Composable
fun ScheduleWidgetLesson(
    lesson: Lesson,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column {
            GlanceText(lesson.timeRange.split(" – ")[0], fontFamily = FontFamily.Monospace)
            GlanceText(lesson.timeRange.split(" – ")[1], fontFamily = FontFamily.Monospace)
        }

        Spacer(GlanceModifier.width(4.dp))
        Box(modifier = GlanceModifier.fillMaxHeight().width(2.dp).background(MaiColor)) {  }
        Spacer(GlanceModifier.width(4.dp))

        Column {
            GlanceText(lesson.name, maxLines = 1)
            Row(verticalAlignment = Alignment.CenterVertically) {
                GlanceText(lesson.type.localizedShortNonContext(), maxLines = 1, fontFamily = FontFamily.Monospace)

                Spacer(GlanceModifier.width(4.dp))
                Box(modifier = GlanceModifier.width(1.dp).background(Color.White.copy(alpha = 0.3f)).fillMaxHeight()) {}
                Spacer(GlanceModifier.width(4.dp))

                GlanceText(lesson.rooms.map { it.name }.joinToString(", "), maxLines = 1)
            }
        }
    }
}

@Composable
fun ScheduleWidgetHeader(
    day: ScheduleDay,
) {
    Row {
        GlanceText("${day.date.dayOfWeek.localizedNonContext()}, ${day.date.dayOfMonth} ${day.date.month.localizedGenitiveNonContext()}", fontSize = 17.sp)
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
fun ScheduleWidgetHeader(
    modifier: GlanceModifier,
) {
    val today = LocalDate.now()
    Column(modifier = modifier) {
        Box(modifier = GlanceModifier.padding(8.dp), contentAlignment = Alignment.Center) {
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.Start,
            ) {
                GlanceTitle("${today.dayOfMonth} ${today.month.localizedGenitiveNonContext()}, ${today.dayOfWeek.localizedNonContext().lowercase()}")
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
                        .clickable(actionRunCallback<RefreshScheduleWidgetAction>())
                        .cornerRadius(4.dp),
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