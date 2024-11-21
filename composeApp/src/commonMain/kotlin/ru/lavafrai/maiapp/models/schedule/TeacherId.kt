package ru.lavafrai.maiapp.models.schedule

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TeacherId(
    @SerialName("name") val name: String,
    @SerialName("uid") val uid: String,
)