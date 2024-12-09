package ru.lavafrai.maiapp.models.schedule

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.lavafrai.maiapp.models.Nameable

@Serializable
data class TeacherId(
    @SerialName("name") override val name: String,
    @SerialName("uid") val uid: String,
): Nameable