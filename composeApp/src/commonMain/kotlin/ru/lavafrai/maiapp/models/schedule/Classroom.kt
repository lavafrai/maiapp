package ru.lavafrai.maiapp.models.schedule

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Classroom(
    @SerialName("name") val name: String,
    @SerialName("uid") val uid: String,
)