package ru.lavafrai.maiapp.navigation.pages

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
class EventCreatePage(
    val initialDate: LocalDate,
)