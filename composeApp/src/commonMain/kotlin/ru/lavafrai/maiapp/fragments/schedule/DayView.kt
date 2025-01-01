package ru.lavafrai.maiapp.fragments.schedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.lavafrai.maiapp.models.annotations.LessonAnnotation
import ru.lavafrai.maiapp.models.exler.ExlerTeacher
import ru.lavafrai.maiapp.models.schedule.ScheduleDay

@Composable
fun DayView(
    day: ScheduleDay,
    modifier: Modifier = Modifier,
    exlerTeachers: List<ExlerTeacher>?,
    annotations: List<LessonAnnotation>,
    scheduleName: String,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier,
    ) {
        for (lesson in day.lessons) {
            LessonView(
                lesson = lesson,
                exlerTeachers = exlerTeachers,
                annotations = annotations.filter { it.lessonUid == lesson.getUid() },
                scheduleName = scheduleName,
            )
        }
    }
}