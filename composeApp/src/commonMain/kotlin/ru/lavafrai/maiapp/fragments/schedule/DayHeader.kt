package ru.lavafrai.maiapp.fragments.schedule

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import compose.icons.FeatherIcons
import compose.icons.feathericons.Plus
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import maiapp.composeapp.generated.resources.Res
import maiapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import ru.lavafrai.maiapp.localizers.localized
import ru.lavafrai.maiapp.localizers.localizedGenitive
import ru.lavafrai.maiapp.models.schedule.ScheduleDay
import ru.lavafrai.maiapp.models.time.now

@Composable
fun DayHeader(
    day: ScheduleDay,
    modifier: Modifier = Modifier,
    showEventAddingButton: Boolean = false,
    onAddEventClick: (() -> Unit) = {},
) {
    val today = day.date == LocalDate.now()
    val tomorrow = day.date == LocalDate.now().plus(1, DateTimeUnit.DAY)

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
                    day.dayOfWeek.localized(),
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(Modifier.width(8.dp))

                when {
                    today -> Surface(
                        modifier = Modifier
                            .align(Alignment.CenterVertically),
                        color = MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.small,
                    ) {
                        Text(
                            stringResource(Res.string.today),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }

                    tomorrow -> Surface(
                        modifier = Modifier
                            .align(Alignment.CenterVertically),
                        color = MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.small,
                    ) {
                        Text(
                            stringResource(Res.string.tomorrow),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }

                    else -> Text(
                        "${day.date.dayOfMonth} ${day.date.month.localizedGenitive()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Light,
                        modifier = Modifier.alpha(0.5f),
                    )
                }
            }
            if (showEventAddingButton) IconButton(onAddEventClick, modifier = Modifier.then(Modifier.size(24.dp))) {
                Icon(FeatherIcons.Plus, contentDescription = "add custom event", modifier = Modifier.alpha(0.5f))
            }
        }
    }
}