@file:OptIn(ExperimentalFoundationApi::class)

package ru.lavafrai.maiapp.fragments.events

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
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
import ru.lavafrai.maiapp.fragments.AppCardShape
import ru.lavafrai.maiapp.fragments.AppCardShapes
import ru.lavafrai.maiapp.fragments.schedule.AnnotationsView
import ru.lavafrai.maiapp.fragments.schedule.PairNumber
import ru.lavafrai.maiapp.localizers.localized
import ru.lavafrai.maiapp.models.annotations.LessonAnnotation
import ru.lavafrai.maiapp.models.events.RenderedEvent
import ru.lavafrai.maiapp.models.exler.ExlerTeacher
import ru.lavafrai.maiapp.models.schedule.Schedule
import ru.lavafrai.maiapp.models.schedule.TeacherUid
import ru.lavafrai.maiapp.theme.LinkColor
import ru.lavafrai.maiapp.utils.conditional
import ru.lavafrai.maiapp.utils.pairNumber
import kotlin.uuid.Uuid


@Composable
fun RenderedEventView(
    event: RenderedEvent,
    exlerTeachers: List<ExlerTeacher>?,
    annotations: List<LessonAnnotation>,
    schedule: Schedule,
    shape: AppCardShape = AppCardShapes.default(),
) {
    val appContext = LocalApplicationContext.current
    val haptic = LocalHapticFeedback.current

    AppCard(
        shape = shape,
        onLongClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)

            appContext.openEventDetails(
                event = event,
                schedule = schedule,
            )
        },
        modifier = Modifier
    ) {
        Row {
            Column { // Pair number and annotations
                PairNumber(text = event.pairNumber)

                val lessonAnnotations = annotations.filter { it.lessonUid == event.getUid() }
                AnnotationsView(
                    annotations = lessonAnnotations,
                    modifier = Modifier.padding(top = 4.dp),
                    onOpenAnnotations = { appContext.openEventDetails(event, schedule) },
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) { // Lesson name, teachers and time
                Text(
                    event.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        lineHeight = 1.2.em,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                )
                Spacer(modifier = Modifier.height(2.dp))

                event.teachers.filter { it.isNotBlank() }.forEach { teacherName ->
                    val areOnExler = remember(exlerTeachers) { exlerTeachers?.any { exler -> exler.name == teacherName } ?: false }
                    val hasSchedule = false // remember(it) { it.uid != TeacherUid.Empty }
                    val clickable = areOnExler || hasSchedule

                    Text(
                        teacherName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (clickable) LinkColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.conditional(clickable) { clickable {
                            if (areOnExler) appContext.openTeacherReviews(teacherName, TeacherUid(Uuid.NIL.toString()))
                            // else if (hasSchedule) appContext.openSchedule(it.uid, it.name)
                        }}
                    )
                }
                // Spacer(modifier = Modifier.height(2.dp))

                Text(
                    event.timeRange,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }

        Row( // Rooms and type
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .height(28.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(event.room.joinToString(separator = "/"), style = MaterialTheme.typography.bodySmall)

            /*DefaultChip(
                onClick = {},
                label = { Text(lesson.type.localized().uppercase(), fontSize = 12.sp) },
                horizontalPadding = 12.dp,
                height = 28.dp,
            )*/
            SuggestionChip(
                onClick = {},
                label = { Text(event.type.localized().uppercase(), fontSize = 13.sp) },
                modifier = Modifier
                    .height(28.dp),
                border = SuggestionChipDefaults.suggestionChipBorder(true, borderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)),
            )
        }
    }
}