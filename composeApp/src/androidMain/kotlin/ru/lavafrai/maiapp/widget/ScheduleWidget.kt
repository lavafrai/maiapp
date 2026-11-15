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
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.AppWidgetId
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
import androidx.glance.layout.ColumnScope
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
import maiapp.composeapp.generated.resources.widget_choose_schedule
import maiapp.composeapp.generated.resources.widget_local_data
import maiapp.composeapp.generated.resources.widget_no_lessons
import maiapp.composeapp.generated.resources.widget_schedule_not_loaded
import maiapp.composeapp.generated.resources.widget_server_check_failed
import maiapp.composeapp.generated.resources.widget_server_data
import maiapp.composeapp.generated.resources.widget_sign_in_required
import maiapp.composeapp.generated.resources.widget_today_title
import org.jetbrains.compose.resources.getString
import ru.lavafrai.maiapp.MainActivity
import ru.lavafrai.maiapp.R
import ru.lavafrai.maiapp.data.repositories.AbstractLessonRepository
import ru.lavafrai.maiapp.data.repositories.ScheduleRepository
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
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

class ScheduleWidget : GlanceAppWidget() {
    private val widgetBackground = Color(0xE0000000)

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val settings = ApplicationSettings.getCurrent()

        // Какие расписания вообще можно показать: сохранённые плюс текущее
        val choices = (listOfNotNull(settings.selectedSchedule) + settings.savedSchedules)
            .distinctBy { it.scheduleId }

        // Что показать: выбранное для этого виджета; если выбора ещё нет или такое расписание удалили —
        // единственное/текущее расписание
        val appWidgetId = (id as? AppWidgetId)?.appWidgetId
        val chosenName = appWidgetId
            ?.let { WidgetScheduleState.scheduleName(context, it) }
            ?.takeIf { name -> choices.any { it.scheduleId == name } }
        val group = when {
            chosenName != null -> BaseScheduleId(chosenName)
            choices.size <= 1 -> choices.firstOrNull()
            else -> null // несколько расписаний — спросим, какое показывать
        }

        // Запоминаем показанное расписание за виджетом: иначе после добавления второго
        // расписания виджет снова предложит выбор
        if (appWidgetId != null && chosenName == null && group != null) {
            WidgetScheduleState.setScheduleName(context, appWidgetId, group.scheduleId)
        }

        Logger.i("Loading schedule ${group?.scheduleId} for widget $id")

        // Когда приложение последний раз загружало это расписание: это и есть возраст локальных данных
        val fetchedAt = group?.let { ScheduleRepository().getFetchedAtFromCacheOrNull(it) } ?: 0L
        val schedule = group?.let { scheduleId ->
            ScheduleWidgetCache.schedule("${scheduleId.scheduleId}:$fetchedAt") {
                AbstractLessonRepository().loadLessonsFromCacheOrNull(scheduleId)
            }
        }

        // The app language, even if its UI hasn't been started in this process yet
        LocalAppLocale.apply(settings.language)

        val labels = WidgetScheduleLabels.load(choices.map { it.scheduleId })
        val serverUpdatedAt = appWidgetId?.let { WidgetScheduleState.serverUpdatedAt(context, it) }
        val serverCheckFailed = appWidgetId?.let { WidgetScheduleState.serverCheckFailed(context, it) } ?: false
        val today = LocalDate.now()

        val strings = ScheduleWidgetCache.strings(
            listOf(
                settings.language,
                today,
                labels,
                schedule?.updatedAt ?: 0L,
                serverUpdatedAt ?: 0L,
                serverCheckFailed,
            )
        ) {
            WidgetStrings.load(
                today = today,
                labels = labels,
                localDataAt = schedule?.updatedAt,
                serverDataAt = serverUpdatedAt,
                serverCheckFailed = serverCheckFailed,
            )
        }

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
                    ScheduleWidgetHeader(group)
                    Box(modifier = GlanceModifier.defaultWeight()) {
                        ScheduleWidgetContent(group, schedule, choices)
                    }
                    ScheduleWidgetFooter()
                }
            }
        }
    }
}

/**
 * Разбор расписания и загрузка строк — дорогие операции, а Glance перерисовывает виджет
 * на каждое действие. Держим результат в памяти и сбрасываем, когда приложение обновило данные.
 */
internal object ScheduleWidgetCache {
    private var scheduleKey: String? = null
    private var scheduleValue: SimpleSchedule? = null
    private var stringsKey: Any? = null
    private var stringsValue: WidgetStrings? = null

    suspend fun schedule(key: String, load: suspend () -> SimpleSchedule?): SimpleSchedule? {
        val cached = scheduleValue
        if (cached != null && scheduleKey == key) return cached

        val loaded = load()
        if (loaded != null) {
            scheduleKey = key
            scheduleValue = loaded
        }
        return loaded
    }

    suspend fun strings(key: Any, load: suspend () -> WidgetStrings): WidgetStrings {
        val cached = stringsValue
        if (cached != null && stringsKey == key) return cached

        val loaded = load()
        stringsKey = key
        stringsValue = loaded
        return loaded
    }

    fun clear() {
        scheduleKey = null
        scheduleValue = null
        stringsKey = null
        stringsValue = null
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
    val labels: Map<String, WidgetScheduleLabel>,
    val noLessons: String,
    val notLoaded: String,
    val signInRequired: String,
    val chooseSchedule: String,
    val localData: String?,
    val serverData: String?,
) {
    companion object {
        suspend fun load(
            today: LocalDate,
            labels: Map<String, WidgetScheduleLabel>,
            localDataAt: Long?,
            serverDataAt: Long?,
            serverCheckFailed: Boolean,
        ): WidgetStrings {
            val weekday = getString(today.dayOfWeek.nameResource)
            return WidgetStrings(
                today = today,
                todayTitle = getString(Res.string.widget_today_title, today.loadDayMonth(), weekday, weekday.lowercase()),
                dayTitles = (0 until 7).associate { dayIndex ->
                    val date = today.plus(DatePeriod(days = dayIndex))
                    date to "${getString(date.dayOfWeek.nameResource)}, ${date.loadDayMonth()}"
                },
                lessonTypes = LessonType.entries.associateWith { getString(it.shortNameResource) },
                labels = labels,
                noLessons = getString(Res.string.widget_no_lessons),
                notLoaded = getString(Res.string.widget_schedule_not_loaded),
                signInRequired = getString(Res.string.widget_sign_in_required),
                chooseSchedule = getString(Res.string.widget_choose_schedule),
                localData = localDataAt?.formatTimestamp()?.let { getString(Res.string.widget_local_data, it) },
                serverData = when {
                    serverCheckFailed -> getString(
                        Res.string.widget_server_data,
                        getString(Res.string.widget_server_check_failed),
                    )
                    serverDataAt != null -> serverDataAt.formatTimestamp()
                        ?.let { getString(Res.string.widget_server_data, it) }
                    else -> null
                },
            )
        }
    }
}

/** Время из кэша приходит то в секундах, то в миллисекундах; показываем «дд.мм.гггг чч:мм:сс» */
private fun Long.formatTimestamp(): String? {
    if (this <= 0L) return null
    val epochMillis = if (this > 100_000_000_000L) this else this * 1000L
    val iso = Instant.fromEpochMilliseconds(epochMillis)
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .toString()
    val date = iso.substringBefore('T')
    val time = iso.substringAfter('T').take(8)
    val parts = date.split('-')
    if (parts.size != 3) return null
    return "${parts[2]}.${parts[1]}.${parts[0]} $time"
}

val LocalWidgetStrings = staticCompositionLocalOf<WidgetStrings> { error("No widget strings provided") }

@Composable
fun ScheduleWidgetContent(
    group: BaseScheduleId?,
    schedule: SimpleSchedule?,
    choices: List<BaseScheduleId> = emptyList(),
) {
    when {
        schedule != null -> ScheduleWidgetSchedule(schedule)
        group != null -> ScheduleWidgetNotLoaded()
        choices.size > 1 -> ScheduleWidgetChooser(choices)
        else -> ScheduleWidgetNotLoggedIn()
    }
}

/**
 * Выбор расписания для этого экземпляра виджета. Показывается, пока выбор не сделан;
 * решение запоминается только для этого виджета, соседние остаются со своими.
 */
@Composable
fun ScheduleWidgetChooser(
    choices: List<BaseScheduleId>,
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 4.dp),
    ) {
        GlanceText(LocalWidgetStrings.current.chooseSchedule, fontSize = 16.sp)
        Spacer(modifier = GlanceModifier.height(4.dp))

        LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
            items(choices, itemId = { it.scheduleId.hashCode().toLong() }) { choice ->
                Column(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable(
                            actionRunCallback<ChooseWidgetScheduleAction>(
                                actionParametersOf(
                                    ChooseWidgetScheduleAction.ScheduleNameKey to choice.scheduleId
                                )
                            )
                        ),
                ) {
                    ScheduleWidgetScheduleName(choice.scheduleId, fontSize = 18.sp)
                }
            }
        }
    }
}

/** Номер расписания и его подпись из настроек: подпись своим цветом, если она задана */
@Composable
fun ScheduleWidgetScheduleName(
    scheduleId: String,
    fontSize: androidx.compose.ui.unit.TextUnit? = null,
) {
    val label = LocalWidgetStrings.current.labels[scheduleId]
    if (label == null) {
        GlanceText(scheduleId, maxLines = 1, fontSize = fontSize)
        return
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        GlanceText(scheduleId, maxLines = 1, fontSize = fontSize)
        Spacer(modifier = GlanceModifier.width(6.dp))
        GlanceText(
            text = label.text,
            maxLines = 1,
            fontSize = fontSize,
            color = widgetLabelColor(label.color) ?: Color.White.copy(alpha = 0.7f),
        )
    }
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
fun ColumnScope.ScheduleWidgetFooter() {
    val strings = LocalWidgetStrings.current
    val lines = listOfNotNull(strings.localData, strings.serverData)
    if (lines.isEmpty()) return

    Column(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 8.dp),
    ) {
        lines.forEach { line ->
            GlanceText(
                text = line,
                maxLines = 1,
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.6f),
            )
        }
    }
}

@Composable
fun ScheduleWidgetHeader(
    group: BaseScheduleId?,
) {
    Column(modifier = GlanceModifier.fillMaxWidth()) {
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = GlanceModifier
                    .defaultWeight()
                    .clickable(actionStartActivity<MainActivity>()),
            ) {
                GlanceTitle(LocalWidgetStrings.current.todayTitle)
                if (group != null) {
                    Row(modifier = GlanceModifier.padding(top = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                        ScheduleWidgetScheduleName(group.scheduleId)
                    }
                }
            }

            Box(
                modifier = GlanceModifier
                    .size(44.dp)
                    .cornerRadius(22.dp)
                    .clickable(actionRunCallback<RefreshScheduleWidgetAction>()),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    ImageProvider(R.drawable.ic_refresh),
                    null,
                    modifier = GlanceModifier.size(26.dp),
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
