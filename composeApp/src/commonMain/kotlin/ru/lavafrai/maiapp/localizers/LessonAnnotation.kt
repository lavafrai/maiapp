package ru.lavafrai.maiapp.localizers

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import compose.icons.FeatherIcons
import compose.icons.LineAwesomeIcons
import compose.icons.feathericons.Zap
import compose.icons.lineawesomeicons.BookSolid
import compose.icons.lineawesomeicons.Comment
import compose.icons.lineawesomeicons.TimesCircle
import compose.icons.lineawesomeicons.TimesSolid
import maiapp.composeapp.generated.resources.Res
import maiapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import ru.lavafrai.maiapp.models.annotations.LessonAnnotation
import ru.lavafrai.maiapp.models.annotations.LessonAnnotationType

fun LessonAnnotation.letter(): String {
    return when(type) {
        LessonAnnotation.FinalTest -> "З"
        LessonAnnotation.ControlWork -> "К"
        LessonAnnotation.HomeWork -> "Д"
        LessonAnnotation.Skipped -> "П"
        LessonAnnotation.Colloquium -> "Q"
        LessonAnnotation.Comment -> "i"
        else -> "Х"
    }
}

fun LessonAnnotation.color(): Color {
    return when(type) {
        LessonAnnotation.FinalTest -> Color(0xFFFF3F3F)
        LessonAnnotation.ControlWork -> Color(0xFFff5c5c)
        LessonAnnotation.Colloquium -> Color(0xffff8fab)
        LessonAnnotation.HomeWork -> Color(0xFFa594f9)
        LessonAnnotation.Comment -> Color(0xffbda88e)
        LessonAnnotation.Skipped -> Color(0xffccdad1)
        else -> Color(0xFFa594f9)
    }
}

fun LessonAnnotation.icon(): ImageVector {
    return when(type) {
        LessonAnnotation.FinalTest -> FeatherIcons.Zap
        LessonAnnotation.ControlWork -> FeatherIcons.Zap
        LessonAnnotation.Colloquium -> FeatherIcons.Zap
        LessonAnnotation.HomeWork -> LineAwesomeIcons.BookSolid
        LessonAnnotation.Comment -> LineAwesomeIcons.Comment
        LessonAnnotation.Skipped -> LineAwesomeIcons.TimesCircle
        else -> LineAwesomeIcons.TimesSolid
    }
}

@Composable
fun LessonAnnotationType.localized(): String {
    return when (this) {
        LessonAnnotation.FinalTest -> stringResource(Res.string.final_test)
        LessonAnnotation.ControlWork -> stringResource(Res.string.control_work)
        LessonAnnotation.Colloquium -> stringResource(Res.string.colloquium)
        LessonAnnotation.HomeWork -> stringResource(Res.string.home_work)
        LessonAnnotation.Comment -> stringResource(Res.string.comment)
        else -> stringResource(Res.string.unknown)
    }
}