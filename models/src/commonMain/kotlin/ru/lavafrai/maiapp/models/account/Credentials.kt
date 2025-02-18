package ru.lavafrai.maiapp.models.account

import kotlinx.serialization.Serializable

@Serializable
data class Credentials(
    val login: String,
    val password: String,
)