package ru.lavafrai.maiapp.localizers

import androidx.compose.runtime.Composable
import maiapp.composeapp.generated.resources.Res
import maiapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import ru.lavafrai.maiapp.models.time.DayOfWeek

/** Also for loading without composition, e.g. with getString */
val DayOfWeek.nameResource: StringResource
    get() = when (this) {
        DayOfWeek.MONDAY -> Res.string.monday
        DayOfWeek.TUESDAY -> Res.string.tuesday
        DayOfWeek.WEDNESDAY -> Res.string.wednesday
        DayOfWeek.THURSDAY -> Res.string.thursday
        DayOfWeek.FRIDAY -> Res.string.friday
        DayOfWeek.SATURDAY -> Res.string.saturday
        DayOfWeek.SUNDAY -> Res.string.sunday
    }

@Composable
fun DayOfWeek.localized(): String = stringResource(nameResource)

@Composable
fun DayOfWeek.localizedShort(): String {
    return when (this) {
        DayOfWeek.MONDAY -> stringResource(Res.string.monday_short)
        DayOfWeek.TUESDAY -> stringResource(Res.string.tuesday_short)
        DayOfWeek.WEDNESDAY -> stringResource(Res.string.wednesday_short)
        DayOfWeek.THURSDAY -> stringResource(Res.string.thursday_short)
        DayOfWeek.FRIDAY -> stringResource(Res.string.friday_short)
        DayOfWeek.SATURDAY -> stringResource(Res.string.saturday_short)
        DayOfWeek.SUNDAY -> stringResource(Res.string.sunday_short)
    }
}

@Composable
fun kotlinx.datetime.DayOfWeek.localized(): String = this.toApplication().localized()

val kotlinx.datetime.DayOfWeek.nameResource: StringResource get() = toApplication().nameResource

@Composable
fun kotlinx.datetime.DayOfWeek.localizedShort(): String = this.toApplication().localizedShort()

fun DayOfWeek.toKotlinx(): kotlinx.datetime.DayOfWeek = when(this) {
    DayOfWeek.MONDAY -> kotlinx.datetime.DayOfWeek.MONDAY
    DayOfWeek.TUESDAY -> kotlinx.datetime.DayOfWeek.TUESDAY
    DayOfWeek.WEDNESDAY -> kotlinx.datetime.DayOfWeek.WEDNESDAY
    DayOfWeek.THURSDAY -> kotlinx.datetime.DayOfWeek.THURSDAY
    DayOfWeek.FRIDAY -> kotlinx.datetime.DayOfWeek.FRIDAY
    DayOfWeek.SATURDAY -> kotlinx.datetime.DayOfWeek.SATURDAY
    DayOfWeek.SUNDAY -> kotlinx.datetime.DayOfWeek.SUNDAY
}

fun kotlinx.datetime.DayOfWeek.toApplication(): DayOfWeek = when(this) {
    kotlinx.datetime.DayOfWeek.MONDAY -> DayOfWeek.MONDAY
    kotlinx.datetime.DayOfWeek.TUESDAY -> DayOfWeek.TUESDAY
    kotlinx.datetime.DayOfWeek.WEDNESDAY -> DayOfWeek.WEDNESDAY
    kotlinx.datetime.DayOfWeek.THURSDAY -> DayOfWeek.THURSDAY
    kotlinx.datetime.DayOfWeek.FRIDAY -> DayOfWeek.FRIDAY
    kotlinx.datetime.DayOfWeek.SATURDAY -> DayOfWeek.SATURDAY
    kotlinx.datetime.DayOfWeek.SUNDAY -> DayOfWeek.SUNDAY
    else -> throw IllegalArgumentException("Unknown day of week: $this")
}
