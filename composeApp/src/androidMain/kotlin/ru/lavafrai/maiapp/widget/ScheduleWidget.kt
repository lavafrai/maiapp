package ru.lavafrai.maiapp.ru.lavafrai.maiapp.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
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
import androidx.glance.appwidget.lazy.items
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
import maiapp.composeapp.generated.resources.Res
import maiapp.composeapp.generated.resources.widget_no_lessons
import maiapp.composeapp.generated.resources.widget_schedule_not_loaded
import maiapp.composeapp.generated.resources.widget_sign_in_required
import maiapp.composeapp.generated.resources.widget_today_title
import org.jetbrains.compose.resources.getString
import ru.lavafrai.maiapp.MainActivity
import ru.lavafrai.maiapp.R
import ru.lavafrai.maiapp.data.repositories.AbstractLessonRepository
import ru.lavafrai.maiapp.data.repositories.SimpleSchedule
import ru.lavafrai.maiapp.data.settings.ApplicationSettings
import ru.lavafrai.maiapp.localizers.LocalAppLocale
import ru.lavafrai.maiapp.localizers.loadDayMonth
import ru.lavafrai.maiapp.localizers.nameResource
import ru.lavafrai.maiapp.localizers.shortNameResource
import ru.lavafrai.maiapp.models.schedule.BaseScheduleId
import ru.lavafrai.maiapp.models.schedule.LessonLike
import ru.lavafrai.maiapp.models.schedule.LessonType
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
        // The app language, even if its UI hasn't been started in this process yet
        LocalAppLocale.apply(settings.language)
        val strings = WidgetStrings.load(today = LocalDate.now())

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
                    ),
                    LocalWidgetStrings provides strings,
                ) {
                    ScheduleWidgetContent(group, schedule)
                }
            }
        }
    }
}

/**
 * Texts of the widget. Compose resources need Compose UI locals to be used in composition, which Glance doesn't
 * have, so the texts are loaded beforehand. The same moment's [today] is used for all of them
 */
class WidgetStrings(
    val today: LocalDate,
    val todayTitle: String,
    val dayTitles: Map<LocalDate, String>,
    val lessonTypes: Map<LessonType, String>,
    val noLessons: String,
    val notLoaded: String,
    val signInRequired: String,
) {
    companion object {
        suspend fun load(today: LocalDate): WidgetStrings {
            val weekday = getString(today.dayOfWeek.nameResource)
            return WidgetStrings(
                today = today,
                todayTitle = getString(Res.string.widget_today_title, today.loadDayMonth(), weekday, weekday.lowercase()),
                dayTitles = (0 until 7).associate { dayIndex ->
                    val date = today.plus(DatePeriod(days = dayIndex))
                    date to "${getString(date.dayOfWeek.nameResource)}, ${date.loadDayMonth()}"
                },
                lessonTypes = LessonType.entries.associateWith { getString(it.shortNameResource) },
                noLessons = getString(Res.string.widget_no_lessons),
                notLoaded = getString(Res.string.widget_schedule_not_loaded),
                signInRequired = getString(Res.string.widget_sign_in_required),
            )
        }
    }
}

val LocalWidgetStrings = staticCompositionLocalOf<WidgetStrings> { error("No widget strings provided") }

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

/**
 * Every row is a separate list item with a stable id and one of a few fixed layouts.
 * Glance truncates containers with many children and reuses item views by layout, so a whole day
 * in one item (with a layout depending on its lessons) got lessons cut off and mixed up.
 */
private sealed class ScheduleWidgetRow(val id: Long) {
    class DayHeader(val date: LocalDate) : ScheduleWidgetRow(date.toEpochDays() * 100L)
    class Lesson(val lesson: LessonLike, date: LocalDate, index: Int) : ScheduleWidgetRow(date.toEpochDays() * 100L + 1 + index)
    class NoLessons(date: LocalDate) : ScheduleWidgetRow(date.toEpochDays() * 100L + 99)
    data object BottomSpacer : ScheduleWidgetRow(0)
}

private fun scheduleWidgetRows(schedule: SimpleSchedule, today: LocalDate): List<ScheduleWidgetRow> = buildList {
    for (dayIndex in 0 until 7) {
        val date = today.plus(DatePeriod(days = dayIndex))
        val lessons = schedule.days[date].orEmpty().sortedBy { it.startTime }

        add(ScheduleWidgetRow.DayHeader(date))
        if (lessons.isEmpty()) add(ScheduleWidgetRow.NoLessons(date))
        lessons.forEachIndexed { index, lesson -> add(ScheduleWidgetRow.Lesson(lesson, date, index)) }
    }
    add(ScheduleWidgetRow.BottomSpacer)
}

@Composable
fun ScheduleWidgetSchedule(
    schedule: SimpleSchedule,
) {
    val rows = scheduleWidgetRows(schedule, LocalWidgetStrings.current.today)

    LazyColumn(modifier = GlanceModifier.padding(horizontal = 8.dp)) {
        items(rows, itemId = { it.id }) { row ->
            when (row) {
                is ScheduleWidgetRow.DayHeader -> ScheduleWidgetDayHeader(row.date)
                is ScheduleWidgetRow.Lesson -> ScheduleWidgetLesson(row.lesson)
                is ScheduleWidgetRow.NoLessons -> ScheduleWidgetNoLessons()
                ScheduleWidgetRow.BottomSpacer -> Spacer(modifier = GlanceModifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ScheduleWidgetNoLessons() {
    Row(modifier = GlanceModifier.padding(start = 8.dp)) {
        GlanceText(LocalWidgetStrings.current.noLessons)
    }
}

@Composable
fun ScheduleWidgetLesson(
    lesson: LessonLike,
) {
    Row(modifier = GlanceModifier.padding(bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
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
                GlanceText(LocalWidgetStrings.current.lessonTypes.getValue(lesson.type), maxLines = 1, fontFamily = FontFamily.Monospace)

                Spacer(GlanceModifier.width(4.dp))
                // Always present, so that the layout is the same for lessons with and without classrooms
                Box(
                    modifier = GlanceModifier
                        .width(1.dp)
                        .background(if (lesson.classrooms.isNotEmpty()) Color.White.copy(alpha = 0.3f) else Color.Transparent)
                        .fillMaxHeight()
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
    Row(modifier = GlanceModifier.padding(top = 8.dp, bottom = 6.dp)) {
        GlanceText(
            LocalWidgetStrings.current.dayTitles.getValue(date),
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
        GlanceTitle(LocalWidgetStrings.current.notLoaded)
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
        GlanceTitle(LocalWidgetStrings.current.signInRequired)
    }
}

@Composable
fun ScheduleWidgetHeader(
    modifier: GlanceModifier,
) {
    Column(modifier = modifier) {
        Box(modifier = GlanceModifier.padding(8.dp), contentAlignment = Alignment.Center) {
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.Start,
            ) {
                GlanceTitle(LocalWidgetStrings.current.todayTitle)
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