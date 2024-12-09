package ru.lavafrai.maiapp.models

import kotlinx.serialization.Serializable


@Serializable
data class Teacher(
    val name: String,
    val groups: Map<String, Int>,
)