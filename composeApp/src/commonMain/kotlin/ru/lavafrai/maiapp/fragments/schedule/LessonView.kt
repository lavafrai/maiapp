package ru.lavafrai.maiapp.fragments.schedule

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import ru.lavafrai.maiapp.LocalApplicationContext
import ru.lavafrai.maiapp.fragments.AppCard
import ru.lavafrai.maiapp.localizers.localized
import ru.lavafrai.maiapp.models.exler.ExlerTeacher
import ru.lavafrai.maiapp.models.schedule.Lesson
import ru.lavafrai.maiapp.theme.LinkColor
import ru.lavafrai.maiapp.utils.conditional

@Composable
fun LessonView(
    lesson: Lesson,
    exlerTeachers: List<ExlerTeacher>?,
) {
    val appContext = LocalApplicationContext.current

    AppCard {
        Row {
            Column {
                PairNumber(text = lesson.getPairNumber().toString())
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
                Text(
                    lesson.timeRange,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )

                lesson.lectors.forEach {
                    val areOnExler = remember(exlerTeachers) { exlerTeachers?.any { exler -> exler.name == it.name } ?: false }
                    Text(
                        it.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (areOnExler) LinkColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.conditional(areOnExler) { clickable { appContext.openTeacherReviews(it.name) } }
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(lesson.rooms.joinToString(separator = "/") { it.name }, style = MaterialTheme.typography.bodySmall)
            AssistChip(onClick = {}, label = { Text(lesson.type.localized().uppercase()) })
        }
    }
}