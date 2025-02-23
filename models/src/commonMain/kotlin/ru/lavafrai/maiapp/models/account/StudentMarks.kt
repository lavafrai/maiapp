package ru.lavafrai.maiapp.models.account

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StudentMarks(
    @SerialName("student") val student: Student,
    @SerialName("recordBook") val recordBook: String,
    @SerialName("marks") val marks: List<Mark>
) {
    fun averageMark(): Double {
        val sum = marks
            .map { it.value }
            .map { it.replace("Ня", "2") }
            .map { it.replace("Нзч", "2") }
            .map { it.replace("Зч", "5") }
            .mapNotNull { it.toDoubleOrNull() }
            .average()
        return sum
    }

    fun debtCount(): Int {
        return marks.count { it.isDebt }
    }
}