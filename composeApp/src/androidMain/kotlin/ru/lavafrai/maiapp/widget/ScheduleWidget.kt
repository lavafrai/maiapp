package ru.lavafrai.maiapp.ru.lavafrai.maiapp.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.FontFamily
import androidx.glance.text.TextDefaults
import androidx.glance.unit.ColorProvider
import co.touchlab.kermit.Logger
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import ru.lavafrai.maiapp.MainActivity
import ru.lavafrai.maiapp.R
import ru.lavafrai.maiapp.data.repositories.AbstractLessonRepository
import ru.lavafrai.maiapp.data.repositories.SimpleSchedule
import ru.lavafrai.maiapp.data.settings.ApplicationSettings
import ru.lavafrai.maiapp.localizers.localizedGenitiveNonContext
import ru.lavafrai.maiapp.localizers.localizedNonContext
import ru.lavafrai.maiapp.localizers.localizedShortNonContext
import ru.lavafrai.maiapp.models.schedule.BaseScheduleId
import ru.lavafrai.maiapp.models.schedule.LessonLike
import ru.lavafrai.maiapp.models.time.now
import ru.lavafrai.maiapp.ru.lavafrai.maiapp.widget.text.GlanceText
import ru.lavafrai.maiapp.ru.lavafrai.maiapp.widget.text.GlanceTitle
import ru.lavafrai.maiapp.ru.lavafrai.maiapp.widget.text.LocalGlanceTextStyle
import ru.lavafrai.maiapp.theme.MaiColor

class ScheduleWidget : GlanceAppWidget() {
    private val widgetBackground = Color(0xE0000000)

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val settings = ApplicationSettings.getCurrent()
        val group = settings.selectedSchedule
        Logger.i("Loading schedule for widget $id")

        val schedule = if (group != null) AbstractLessonRepository().loadLessonsFromCacheOrNull(group) else null

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .cornerRadius(26.dp)
                    .appWidgetBackground()
                    .background(widgetBackground),
            ) {
                CompositionLocalProvider(
                    LocalGlanceTextStyle provides TextDefaults.defaultTextStyle.copy(
                        color = ColorProvider(Color.White),
                        fontSize = 14.sp,
                    )
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
    schedule: SimpleSchedule?,
) {
    ScheduleWidgetHeader(modifier = GlanceModifier.clickable(actionStartActivity<MainActivity>()))
    if (schedule != null) ScheduleWidgetSchedule(schedule)
    else if (group != null) ScheduleWidgetNotLoaded()
    else ScheduleWidgetNotLoggedIn()
}

@Composable
fun ScheduleWidgetSchedule(
    schedule: SimpleSchedule,
) {
    val today = LocalDate.now()
    val filteredDays = schedule.days
        .filter { it.key >= today }
        .filter { it.key < today.plus(DatePeriod(days = 7)) }
        .filter { it.value.isNotEmpty() }

    LazyColumn(modifier = GlanceModifier.padding(horizontal = 8.dp)) {
        items(7) { dayIndex ->
            val date = today.plus(DatePeriod(days = dayIndex))
            val lessons = filteredDays[date] ?: emptyList()

            Column {
                Spacer(modifier = GlanceModifier.height(8.dp))
                ScheduleWidgetDay(date, lessons)
            }
        }
        item {
            Spacer(modifier = GlanceModifier.height(16.dp))
        }
    }
}

@Composable
fun ScheduleWidgetDay(
    date: LocalDate,
    lessons: List<LessonLike>,
) {
    Column {
        Column {
            ScheduleWidgetDayHeader(date)
            Spacer(modifier = GlanceModifier.height(6.dp))
        }


        if (lessons.isNotEmpty())
            Column {
                lessons.sortedBy { it.startTime }.forEach {
                    Column {
                        ScheduleWidgetLesson(it)
                        Spacer(modifier = GlanceModifier.height(4.dp))
                    }
                }
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
    lesson: LessonLike,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(horizontalAlignment = Alignment.End) {
            GlanceText(lesson.startTimePaddedString, fontFamily = FontFamily.Monospace)
            GlanceText(lesson.endTimePaddedString, fontFamily = FontFamily.Monospace)
        }

        Spacer(GlanceModifier.width(4.dp))
        Box(modifier = GlanceModifier.fillMaxHeight().width(2.dp).background(MaiColor)) { }
        Spacer(GlanceModifier.width(4.dp))

        Column {
            GlanceText(lesson.name, maxLines = 1)
            Row(verticalAlignment = Alignment.CenterVertically) {
                GlanceText(lesson.type.localizedShortNonContext(), maxLines = 1, fontFamily = FontFamily.Monospace)

                Spacer(GlanceModifier.width(4.dp))
                if (lesson.classrooms.isNotEmpty()) Box(
                    modifier = GlanceModifier.width(1.dp).background(Color.White.copy(alpha = 0.3f)).fillMaxHeight()
                ) {}
                Spacer(GlanceModifier.width(4.dp))

                GlanceText(lesson.classrooms.joinToString(", "), maxLines = 1)
            }
        }
    }
}

@Composable
fun ScheduleWidgetDayHeader(
    date: LocalDate,
) {
    Row {
        GlanceText(
            "${date.dayOfWeek.localizedNonContext()}, ${date.dayOfMonth} ${date.month.localizedGenitiveNonContext()}",
            fontSize = 17.sp
        )
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
                GlanceTitle(
                    "${today.dayOfMonth} ${today.month.localizedGenitiveNonContext()}, ${
                        today.dayOfWeek.localizedNonContext().lowercase()
                    }"
                )
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