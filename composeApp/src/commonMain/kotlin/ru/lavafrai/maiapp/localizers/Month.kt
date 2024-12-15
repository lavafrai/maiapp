package ru.lavafrai.maiapp.localizers

import androidx.compose.runtime.Composable
import kotlinx.datetime.Month
import maiapp.composeapp.generated.resources.Res
import maiapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun Month.localized(): String {
    return when(this) {
        Month.JANUARY -> stringResource(Res.string.january)
        Month.FEBRUARY -> stringResource(Res.string.february)
        Month.MARCH -> stringResource(Res.string.march)
        Month.APRIL -> stringResource(Res.string.april)
        Month.MAY -> stringResource(Res.string.may)
        Month.JUNE -> stringResource(Res.string.june)
        Month.JULY -> stringResource(Res.string.july)
        Month.AUGUST -> stringResource(Res.string.august)
        Month.SEPTEMBER -> stringResource(Res.string.september)
        Month.OCTOBER -> stringResource(Res.string.october)
        Month.NOVEMBER -> stringResource(Res.string.november)
        Month.DECEMBER -> stringResource(Res.string.december)
        else -> throw IllegalArgumentException("Unknown month: $this")
    }
}

@Composable
fun Month.localizedGenitive(): String {
    return when(this) {
        Month.JANUARY -> stringResource(Res.string.january_genitive)
        Month.FEBRUARY -> stringResource(Res.string.february_genitive)
        Month.MARCH -> stringResource(Res.string.march_genitive)
        Month.APRIL -> stringResource(Res.string.april_genitive)
        Month.MAY -> stringResource(Res.string.may_genitive)
        Month.JUNE -> stringResource(Res.string.june_genitive)
        Month.JULY -> stringResource(Res.string.july_genitive)
        Month.AUGUST -> stringResource(Res.string.august_genitive)
        Month.SEPTEMBER -> stringResource(Res.string.september_genitive)
        Month.OCTOBER -> stringResource(Res.string.october_genitive)
        Month.NOVEMBER -> stringResource(Res.string.november_genitive)
        Month.DECEMBER -> stringResource(Res.string.december_genitive)
        else -> throw IllegalArgumentException("Unknown month: $this")
    }
}