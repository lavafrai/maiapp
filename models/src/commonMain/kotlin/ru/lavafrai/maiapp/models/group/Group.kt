package ru.lavafrai.maiapp.models.group

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.lavafrai.maiapp.models.Nameable

@Serializable
data class Group (
    @SerialName("name") override val name: String,
    @SerialName("fac") val faculty: String? = null,
    @SerialName("level") val educationLevel: EducationLevel? = null,
): Nameable