package ru.lavafrai.maiapp.models.events

import kotlinx.serialization.Serializable


@Serializable
enum class EventType {
    Lecture,
    Laboratory,
    Seminar,
    Exam,
    ControlWork,
    FinalTest,
    Meeting,
    Other,
}