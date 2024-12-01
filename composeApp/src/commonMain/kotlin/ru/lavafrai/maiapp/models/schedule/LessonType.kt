package ru.lavafrai.maiapp.models.schedule

import androidx.compose.runtime.Composable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import maiapp.composeapp.generated.resources.Res
import maiapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource


@Serializable
enum class LessonType {
    @SerialName("ЛК") LECTURE,
    @SerialName("ЛР") LABORATORY,
    @SerialName("ПЗ") SEMINAR,
    @SerialName("Экзамен") EXAM,
    @SerialName("") UNKNOWN,
    ;
}

@Composable
fun LessonType.localized(): String {
    return when (this) {
        LessonType.LECTURE -> stringResource(Res.string.lecture)
        LessonType.LABORATORY -> stringResource(Res.string.laboratory)
        LessonType.SEMINAR -> stringResource(Res.string.seminar)
        LessonType.EXAM -> stringResource(Res.string.exam)
        LessonType.UNKNOWN -> stringResource(Res.string.unknown)
    }
}