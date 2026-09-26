package ru.lavafrai.maiapp.models.account

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Mark(
    @SerialName("name") val name: String,
    @SerialName("mark") val value: String,
    @SerialName("attempts") val attempts: Int,
    @SerialName("typeControlName") val typeControlName: String,
    @SerialName("semester") val semester: Int,
    @SerialName("course") val course: Int,
    @SerialName("hours") val hours: Int,
    @SerialName("lecturer") val lecturer: String,
) {
    val isDebt
        get() = value.trim().lowercase() in debtMarks

    val isSuccess
        get() = value in listOf("Зч", "3", "4", "5")

    private companion object {
        // Незачёт, неявка, двойка, недопуск кафедрой, недопуск деканатом
        val debtMarks = setOf("нзч", "ня", "2", "нк", "нд")
    }
}