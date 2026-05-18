package ru.lavafrai.maiapp.fragments.schedule

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import compose.icons.FeatherIcons
import compose.icons.feathericons.Gift
import maiapp.composeapp.generated.resources.Res
import maiapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import ru.lavafrai.maiapp.models.time.DateRange

@Composable
fun EmptyScheduleView(
    dateRange: DateRange?,
) {
    Column(Modifier.fillMaxWidth(0.7f), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(FeatherIcons.Gift, "week is empty")
        Spacer(Modifier.height(16.dp))
        if (dateRange != null) Text(dateRange.toString(), modifier = Modifier.alpha(1f), fontWeight = FontWeight.Light)
        Text(stringResource(Res.string.schedule_is_empty), textAlign = TextAlign.Center)
    }
}