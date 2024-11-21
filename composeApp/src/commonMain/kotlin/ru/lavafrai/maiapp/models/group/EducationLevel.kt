package ru.lavafrai.maiapp.models.group

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
enum class EducationLevel {
    @SerialName("Бакалавриат") BACHELOR,
    @SerialName("Магистратура") MAGISTRACY,
    @SerialName("Аспирантура") POSTGRADUATE, // Аспирантура

    @SerialName("Специалитет") SPECIAL,
    @SerialName("Базовое высшее образование") BASE_HIGH,
    @SerialName("Специализированное высшее образование") SPECIAL_HIGH,
    @SerialName("") SERVICE,
}