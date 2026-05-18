package ru.lavafrai.maiapp.models.exler

import kotlinx.serialization.Serializable

@Serializable
data class ExlerFaculty(
    val name: String,
    val path: String,
)