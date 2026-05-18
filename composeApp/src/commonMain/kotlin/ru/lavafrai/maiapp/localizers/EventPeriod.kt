package ru.lavafrai.maiapp.localizers

import androidx.compose.runtime.Composable
import maiapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import ru.lavafrai.maiapp.models.events.SimpleEventPeriod


@Composable
fun SimpleEventPeriod.localized(): String {
    return when (this) {
        SimpleEventPeriod.Single -> stringResource(Res.string.event_period_single)
        SimpleEventPeriod.Weekly -> stringResource(Res.string.event_period_weekly)
        SimpleEventPeriod.Biweekly -> stringResource(Res.string.event_period_biweekly)
        SimpleEventPeriod.Monthly -> stringResource(Res.string.event_period_monthly)
    }
}

@Composable
fun SimpleEventPeriod.localizedBeforeTime(): String {
    return when (this) {
        SimpleEventPeriod.Single -> stringResource(Res.string.event_period_single_before)
        SimpleEventPeriod.Weekly -> stringResource(Res.string.event_period_weekly_before)
        SimpleEventPeriod.Biweekly -> stringResource(Res.string.event_period_biweekly_before)
        SimpleEventPeriod.Monthly -> stringResource(Res.string.event_period_monthly_before)
    }
}