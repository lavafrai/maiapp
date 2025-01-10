@file:OptIn(ExperimentalFoundationApi::class)

package ru.lavafrai.maiapp.fragments.schedule

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import ru.lavafrai.maiapp.LocalApplicationContext
import ru.lavafrai.maiapp.fragments.AppCard
import ru.lavafrai.maiapp.localizers.localized
import ru.lavafrai.maiapp.models.annotations.LessonAnnotation
import ru.lavafrai.maiapp.models.exler.ExlerTeacher
import ru.lavafrai.maiapp.models.schedule.Lesson
import ru.lavafrai.maiapp.models.schedule.Schedule
import ru.lavafrai.maiapp.theme.LinkColor
import ru.lavafrai.maiapp.utils.conditional

@Composable
fun LessonView(
    lesson: Lesson,
    exlerTeachers: List<ExlerTeacher>?,
    annotations: List<LessonAnnotation>,
    schedule: Schedule,
) {
    val appContext = LocalApplicationContext.current
    val haptic = LocalHapticFeedback.current

    AppCard(
        onLongClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)

            appContext.openLessonDetails(
                lesson = lesson,
                schedule = schedule,
            )
        },
    ) {
        Row {
            Column {
                PairNumber(text = lesson.getPairNumber().toString())

                val lessonAnnotations = annotations.filter { it.lessonUid == lesson.getUid() }
                AnnotationsView(
                    annotations = lessonAnnotations,
                    modifier = Modifier.padding(top = 4.dp),
                    onOpenAnnotations = { appContext.openLessonDetails(lesson, schedule) },
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Text(
                    lesson.name,
                    style = MaterialTheme.typography.titleMedium.copy(lineHeight = 1.2.em, fontSize = 17.sp, fontWeight = FontWeight.Medium),
                )
                Spacer(modifier = Modifier.height(8.dp))

                lesson.lectors.forEach {
                    val areOnExler = remember(exlerTeachers) { exlerTeachers?.any { exler -> exler.name == it.name } ?: false }
                    Text(
                        it.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (areOnExler) LinkColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.conditional(areOnExler) { clickable { appContext.openTeacherReviews(it.name) } }
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    lesson.timeRange,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .height(32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(lesson.rooms.joinToString(separator = "/") { it.name }, style = MaterialTheme.typography.bodySmall)

            SuggestionChip(
                onClick = {},
                label = { Text(lesson.type.localized().uppercase()) },
                //modifier = Modifier.padding(end = 8.dp)
            )
        }
    }
}