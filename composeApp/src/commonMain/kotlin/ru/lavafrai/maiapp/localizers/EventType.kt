package ru.lavafrai.maiapp.localizers

import androidx.compose.runtime.Composable
import maiapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import ru.lavafrai.maiapp.models.events.EventType

@Composable
fun EventType.localized(): String {
    return when (this) {
        EventType.Lecture -> stringResource(Res.string.lecture)
        EventType.Laboratory -> stringResource(Res.string.laboratory)
        EventType.Seminar -> stringResource(Res.string.seminar)
        EventType.Exam -> stringResource(Res.string.exam)
        EventType.ControlWork -> stringResource(Res.string.control_work)
        EventType.FinalTest -> stringResource(Res.string.final_test)
        EventType.Meeting -> stringResource(Res.string.meeting)
        EventType.Other -> stringResource(Res.string.other)
    }
}