package ru.lavafrai.maiapp.localizers

import androidx.compose.runtime.Composable
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

@Composable
fun LessonType.localizedShortNonContext() = when (this) {
    LessonType.LECTURE -> "ЛК"
    LessonType.LABORATORY -> "ЛР"
    LessonType.SEMINAR -> "ПЗ"
    LessonType.EXAM -> "ЭК"
    LessonType.UNKNOWN -> "ХЗ"
}

@Composable
fun String.localizeTypeControlName(): String {
    return when(this) {
        "Зч" -> stringResource(Res.string.assessment)
        "Зо" -> stringResource(Res.string.assessment_with_mark)
        "Э" -> stringResource(Res.string.exam)
        "Р" -> stringResource(Res.string.rating)
        "КР" -> stringResource(Res.string.coursework)
        "КП" -> stringResource(Res.string.courseproject)

        else -> stringResource(Res.string.unknown)
    }
}