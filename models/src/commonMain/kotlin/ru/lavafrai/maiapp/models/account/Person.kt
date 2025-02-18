package ru.lavafrai.maiapp.models.account

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Person(
    @SerialName("citizenship") val citizenship: String,
    @SerialName("code") val code: String,
    @SerialName("compatriot") val compatriot: Boolean,
    @SerialName("dateOfBirth") val dateOfBirth: String,
    @SerialName("firstname") val firstname: String,
    @SerialName("lastname") val lastname: String,
    @SerialName("middlename") val middlename: String,
    @SerialName("gender") val gender: Gender,
    @SerialName("phone") val phone: String,
    @SerialName("snils") val snils: String,
)