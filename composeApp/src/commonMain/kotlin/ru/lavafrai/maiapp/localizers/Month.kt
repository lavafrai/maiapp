package ru.lavafrai.maiapp.localizers

import androidx.compose.runtime.Composable
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import maiapp.composeapp.generated.resources.Res
import maiapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
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

/** Also for loading without composition, e.g. with getString */
val Month.genitiveNameResource: StringResource
    get() = when (this) {
        Month.JANUARY -> Res.string.january_genitive
        Month.FEBRUARY -> Res.string.february_genitive
        Month.MARCH -> Res.string.march_genitive
        Month.APRIL -> Res.string.april_genitive
        Month.MAY -> Res.string.may_genitive
        Month.JUNE -> Res.string.june_genitive
        Month.JULY -> Res.string.july_genitive
        Month.AUGUST -> Res.string.august_genitive
        Month.SEPTEMBER -> Res.string.september_genitive
        Month.OCTOBER -> Res.string.october_genitive
        Month.NOVEMBER -> Res.string.november_genitive
        Month.DECEMBER -> Res.string.december_genitive
        else -> throw IllegalArgumentException("Unknown month: $this")
    }

@Composable
fun Month.localizedGenitive(): String = stringResource(genitiveNameResource)

/** "28 сентября" or "September 28": the order depends on the language */
@Composable
fun LocalDate.localizedDayMonth(withYear: Boolean = false): String =
    if (withYear) stringResource(Res.string.date_day_month_year, dayOfMonth, month.localizedGenitive(), year)
    else stringResource(Res.string.date_day_month, dayOfMonth, month.localizedGenitive())

/** [localizedDayMonth] without composition, e.g. for the widget */
suspend fun LocalDate.loadDayMonth(): String =
    getString(Res.string.date_day_month, dayOfMonth, getString(month.genitiveNameResource))
