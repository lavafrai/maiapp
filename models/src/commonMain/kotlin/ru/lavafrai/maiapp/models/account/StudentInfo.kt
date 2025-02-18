package ru.lavafrai.maiapp.models.account

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StudentInfo(
    @SerialName("firstname") val firstname: String,
    @SerialName("lastname") val lastname: String,
    @SerialName("middlename") val middlename: String,
    @SerialName("login") val login: String,
    @SerialName("students") val students: List<Student>
)