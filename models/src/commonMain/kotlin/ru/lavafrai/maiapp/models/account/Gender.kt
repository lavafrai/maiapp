package ru.lavafrai.maiapp.models.account

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Gender {
    @SerialName("ML") Male,
    @SerialName("FM") Female,
}