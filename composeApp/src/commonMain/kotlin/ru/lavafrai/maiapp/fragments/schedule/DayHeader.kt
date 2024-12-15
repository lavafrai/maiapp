package ru.lavafrai.maiapp.fragments.schedule

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.lavafrai.maiapp.localizers.localized
import ru.lavafrai.maiapp.localizers.localizedGenitive
import ru.lavafrai.maiapp.models.schedule.ScheduleDay

@Composable
fun DayHeader(
    day: ScheduleDay,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                day.dayOfWeek.localized(),
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "${day.date.dayOfMonth} ${day.date.month.localizedGenitive()}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Light,
                modifier = Modifier.alpha(0.5f),
            )
        }
    }
}