package ru.lavafrai.maiapp.localizers

import androidx.compose.runtime.Composable
import maiapp.composeapp.generated.resources.Res
import maiapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import ru.lavafrai.maiapp.models.schedule.LessonType

@Composable
fun LessonType.localized() = when (this) {
    LessonType.LECTURE -> stringResource(Res.string.lecture)
    LessonType.LABORATORY -> stringResource(Res.string.laboratory)
    LessonType.SEMINAR -> stringResource(Res.string.seminar)
    LessonType.EXAM -> stringResource(Res.string.exam)
    LessonType.UNKNOWN -> stringResource(Res.string.unknown)
}