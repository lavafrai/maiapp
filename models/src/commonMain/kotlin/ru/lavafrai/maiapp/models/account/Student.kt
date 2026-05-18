package ru.lavafrai.maiapp.models.account

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Student(
    @SerialName("id") val id: Int,
    @SerialName("studentCode") val studentCode: String,
    @SerialName("department") val department: String,
    @SerialName("speciality") val speciality: String,
    @SerialName("specialityCipher") val specialityCipher: String,
    @SerialName("educationLevel") val educationLevel: String,
    @SerialName("group") val group: String,
    @SerialName("course") val course: String,
    @SerialName("statusName") val statusName: String,
)