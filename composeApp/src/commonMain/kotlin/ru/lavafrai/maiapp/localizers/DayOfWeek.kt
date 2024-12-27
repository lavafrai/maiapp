package ru.lavafrai.maiapp.localizers

import androidx.compose.runtime.Composable
import maiapp.composeapp.generated.resources.Res
import maiapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import ru.lavafrai.maiapp.models.time.DayOfWeek

@Composable
fun DayOfWeek.localized(): String {
    return when (this) {
        DayOfWeek.MONDAY -> stringResource(Res.string.monday)
        DayOfWeek.TUESDAY -> stringResource(Res.string.tuesday)
        DayOfWeek.WEDNESDAY -> stringResource(Res.string.wednesday)
        DayOfWeek.THURSDAY -> stringResource(Res.string.thursday)
        DayOfWeek.FRIDAY -> stringResource(Res.string.friday)
        DayOfWeek.SATURDAY -> stringResource(Res.string.saturday)
        DayOfWeek.SUNDAY -> stringResource(Res.string.sunday)
        else -> throw IllegalArgumentException("Unknown day of week: $this")
    }
}

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
        else -> throw IllegalArgumentException("Unknown day of week: $this")
    }
}

@Composable
fun kotlinx.datetime.DayOfWeek.localized(): String = this.toApplication().localized()

@Composable
fun kotlinx.datetime.DayOfWeek.localizedNonContext(): String = this.toApplication().localizedNonContext()

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



@Composable
fun DayOfWeek.localizedNonContext(): String {
    return when (this) {
        DayOfWeek.MONDAY -> "Понедельник"
        DayOfWeek.TUESDAY -> "Вторник"
        DayOfWeek.WEDNESDAY -> "Среда"
        DayOfWeek.THURSDAY -> "Четверг"
        DayOfWeek.FRIDAY -> "Пятница"
        DayOfWeek.SATURDAY -> "Суббота"
        DayOfWeek.SUNDAY -> "Воскресенье"
        else -> throw IllegalArgumentException("Unknown day of week: $this")
    }
}