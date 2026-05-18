package ru.lavafrai.maiapp.models.account

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Applicants(
    @SerialName("person") val person: Person? = null,
)