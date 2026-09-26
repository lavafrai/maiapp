package ru.lavafrai.maiapp.localizers

import androidx.compose.runtime.Composable
import maiapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import ru.lavafrai.maiapp.models.schedule.LessonType

@Composable
fun LessonType.localized() = when (this) {
    LessonType.LECTURE -> stringResource(Res.string.lecture)
    LessonType.LABORATORY -> stringResource(Res.string.laboratory)
    LessonType.SEMINAR -> stringResource(Res.string.seminar)
    LessonType.EXAM -> stringResource(Res.string.exam)
    LessonType.MEETING -> stringResource(Res.string.meeting)
    LessonType.OTHER -> stringResource(Res.string.other)
}

/** Also for loading without composition, e.g. with getString */
val LessonType.shortNameResource: StringResource
    get() = when (this) {
        LessonType.LECTURE -> Res.string.lecture_short
        LessonType.LABORATORY -> Res.string.laboratory_short
        LessonType.SEMINAR -> Res.string.seminar_short
        LessonType.EXAM -> Res.string.exam_short
        LessonType.MEETING -> Res.string.meeting_short
        LessonType.OTHER -> Res.string.other_short
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