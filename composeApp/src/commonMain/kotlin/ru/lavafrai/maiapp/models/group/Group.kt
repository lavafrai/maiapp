package ru.lavafrai.maiapp.models.group

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Group (
    @SerialName("name") val name: String,
    @SerialName("fac") val faculty: String? = null,
    @SerialName("level") val educationLevel: EducationLevel? = null,
)