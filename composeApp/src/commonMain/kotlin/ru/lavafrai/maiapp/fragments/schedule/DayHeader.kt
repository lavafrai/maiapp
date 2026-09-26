package ru.lavafrai.maiapp.fragments.schedule

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import maiapp.composeapp.generated.resources.Res
import maiapp.composeapp.generated.resources.in_days
import maiapp.composeapp.generated.resources.today
import maiapp.composeapp.generated.resources.tomorrow
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import ru.lavafrai.maiapp.localizers.localized
import ru.lavafrai.maiapp.localizers.localizedDayMonth
import ru.lavafrai.maiapp.models.time.now

@Composable
fun DayHeader(
    date: LocalDate,
    modifier: Modifier = Modifier,
    showEventAddingButton: Boolean = false,
    onAddEventClick: (() -> Unit) = {},
    nearest: Boolean = false,
    showCountdown: Boolean = false,
) {
    val today = date == LocalDate.now()
    val tomorrow = date == LocalDate.now().plus(1, DateTimeUnit.DAY)
    val dateText = date.localizedDayMonth()
    val daysUntil = date.toEpochDays() - LocalDate.now().toEpochDays()

    // Shown in a frame instead of the plain date
    val highlightedText = when {
        today -> stringResource(Res.string.today)
        tomorrow -> stringResource(Res.string.tomorrow)
        nearest -> dateText
        showCountdown && daysUntil > 1 -> pluralStringResource(Res.plurals.in_days, daysUntil, daysUntil)
        else -> null
    }

    Column(
        modifier = modifier
            .fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    date.dayOfWeek.localized(),
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(Modifier.width(8.dp))

                if (highlightedText != null) Surface(
                    modifier = Modifier
                        .align(Alignment.CenterVertically),
                    color = MaterialTheme.colorScheme.primary,
                    shape = MaterialTheme.shapes.small,
                ) {
                    Text(
                        highlightedText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                } else Text(
                    dateText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Light,
                    modifier = Modifier.alpha(0.5f),
                )
            }
            // TODO: add event adding button
            /* if (showEventAddingButton) IconButton(onAddEventClick, modifier = Modifier.then(Modifier.size(24.dp))) {
                Icon(FeatherIcons.Plus, contentDescription = "add custom event", modifier = Modifier.alpha(0.5f))
            } */
        }
    }
}