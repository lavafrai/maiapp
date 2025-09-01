package ru.lavafrai.maiapp.fragments.events

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.lavafrai.maiapp.fragments.AppCard
import ru.lavafrai.maiapp.localizers.localized
import ru.lavafrai.maiapp.models.events.SimpleEvent
import ru.lavafrai.maiapp.models.events.SimpleEventPeriod
import ru.lavafrai.maiapp.models.events.toHalfPaddedString

// Ключи для shared transition во избежание magic string
private const val SharedKeyName = "nameText"
private const val SharedKeyPeriod = "periodText"

@Composable
fun SimpleEventEditorCard(
    event: SimpleEvent,
    expanded: Boolean = false,
    onExpandedChange: (isExpanded: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(
        onClick = { onExpandedChange(!expanded) },
        modifier = modifier,
    ) {
        SharedTransitionLayout(modifier = modifier) {
            AnimatedContent(targetState = expanded) { expandedState ->
                if (expandedState) {
                    SimpleEventEditorCardExpanded(
                        event = event,
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedContentScope = this@AnimatedContent,
                    )
                } else {
                    SimpleEventEditorCardClosed(
                        event = event,
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedContentScope = this@AnimatedContent,
                    )
                }
            }
        }
    }
}

@Composable
fun SimpleEventEditorCardClosed(
    event: SimpleEvent,
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
) = with(sharedTransitionScope) {
    val periodText = event.period.localized().lowercase()
    Row(modifier = modifier) {
        Text(
            event.name,
            style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onBackground),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            modifier = Modifier
                .weight(1f, fill = false)
                .sharedElement(rememberSharedContentState(SharedKeyName), animatedContentScope),
        )
        Text(" - ", style = MaterialTheme.typography.titleMedium.copy(color = LocalContentColor.current.copy(alpha = 0.7f)))
        Text(
            periodText,
            style = MaterialTheme.typography.titleMedium.copy(color = LocalContentColor.current.copy(alpha = 0.7f)),
            maxLines = 1,
            modifier = Modifier.sharedElement(rememberSharedContentState(SharedKeyPeriod), animatedContentScope)
        )
    }
}

@Composable
fun SimpleEventEditorCardExpanded(
    event: SimpleEvent,
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
) = with(sharedTransitionScope) {
    val periodText = event.period.localized().lowercase()
    val timeRange = "${event.startTime.toHalfPaddedString()} – ${event.endTime.toHalfPaddedString()}"
    val dateOrRange = event.formattedDateOrRange()
    val rooms = event.room.filter { it.isNotBlank() }
    val teachers = event.teachers.filter { it.isNotBlank() }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            event.name,
            style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold),
            modifier = Modifier.sharedElement(rememberSharedContentState(SharedKeyName), animatedContentScope)
        )
        Spacer(Modifier.height(8.dp))

        Text(timeRange, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                periodText,
                style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium),
                modifier = Modifier.sharedElement(rememberSharedContentState(SharedKeyPeriod), animatedContentScope)
            )
            Text("  •  $dateOrRange", style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
        }

        Spacer(Modifier.height(8.dp))
        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
        Spacer(Modifier.height(8.dp))

        if (rooms.isNotEmpty()) {
            LabeledSection(title = "Аудитории") {
                Text(rooms.joinToString(", "), style = MaterialTheme.typography.bodyLarge)
            }
            Spacer(Modifier.height(16.dp))
        }

        if (teachers.isNotEmpty()) {
            LabeledSection(title = "Преподаватели") {
                Column { teachers.forEach { teacher -> Text(teacher, style = MaterialTheme.typography.bodyLarge) } }
            }
        }

        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            SuggestionChip(
                onClick = {},
                enabled = false,
                label = { Text(event.eventType.localized().uppercase(), style = MaterialTheme.typography.labelLarge) },
                modifier = Modifier.height(32.dp),
                border = SuggestionChipDefaults.suggestionChipBorder(
                    enabled = false,
                    borderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            )
        }
    }
}

@Composable
private fun LabeledSection(title: String, content: @Composable () -> Unit) {
    Text(
        title,
        style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
    )
    Spacer(Modifier.height(4.dp))
    content()
}

// Вспомогательные функции форматирования (простые, без локали)
private fun formatDate(date: kotlinx.datetime.LocalDate): String = listOf(
    date.dayOfMonth.toString().padStart(2, '0'),
    date.monthNumber.toString().padStart(2, '0'),
    date.year.toString()
).joinToString(".")

private fun formatDateRange(start: kotlinx.datetime.LocalDate, end: kotlinx.datetime.LocalDate): String =
    if (start == end) formatDate(start) else "${formatDate(start)} – ${formatDate(end)}"

// Расширение для более чистого получения строки даты/диапазона
private fun SimpleEvent.formattedDateOrRange(): String = when (period) {
    SimpleEventPeriod.Single -> formatDate(date)
    else -> endDate?.let { formatDateRange(date, it) } ?: formatDate(date)
}
