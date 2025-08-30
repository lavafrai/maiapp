package ru.lavafrai.maiapp.fragments.schedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import ru.lavafrai.maiapp.fragments.AppCardShapes
import ru.lavafrai.maiapp.fragments.events.RenderedEventView
import ru.lavafrai.maiapp.models.annotations.LessonAnnotation
import ru.lavafrai.maiapp.models.events.RenderedEvent
import ru.lavafrai.maiapp.models.exler.ExlerTeacher
import ru.lavafrai.maiapp.models.schedule.Lesson
import ru.lavafrai.maiapp.models.schedule.LessonLike
import ru.lavafrai.maiapp.models.schedule.Schedule

@Composable
fun DayView(
    date: LocalDate,
    lessons: List<LessonLike>,
    modifier: Modifier = Modifier,
    exlerTeachers: List<ExlerTeacher>?,
    annotations: List<LessonAnnotation>,
    schedule: Schedule,
    // events: List<RenderedEvent>,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = modifier,
    ) {
        val sortedLessons = remember(lessons) { lessons.sortedBy { it.startTime } }
        //val sortedLessons = remember(day.lessons) { day.lessons.sortedBy { it.timeStart.toLocalTime() } }
        //val sortedEvents = remember(events) { events.filter { it.date == day.date }.sortedBy { it.startTime } }
        //val sortedLessonsWithEvents = remember(sortedLessons, sortedEvents) {(sortedLessons + sortedEvents).sortedBy { it.startTime }}

        for (lessonId in sortedLessons.indices) {
            val lesson = sortedLessons[lessonId]

            val first = lessonId == 0
            val last = lessonId == sortedLessons.lastIndex
            val shape = when {
                first && last -> AppCardShapes.firstLast()
                first -> AppCardShapes.first()
                last -> AppCardShapes.last()
                else -> AppCardShapes.middle()
            }

            if (lesson is Lesson) LessonView(
                lesson = lesson,
                exlerTeachers = exlerTeachers,
                annotations = annotations.filter { it.lessonUid == lesson.getUid() },
                schedule = schedule,
                shape = shape,
            )
            if (lesson is RenderedEvent) RenderedEventView(
                event = lesson,
                exlerTeachers = exlerTeachers,
                annotations = emptyList(),
                schedule = schedule,
                shape = shape,
            )
        }
    }
}