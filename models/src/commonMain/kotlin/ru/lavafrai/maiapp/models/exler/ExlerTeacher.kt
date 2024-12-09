package ru.lavafrai.maiapp.models.exler

import kotlinx.serialization.Serializable

@Serializable
data class ExlerTeacher(
    val name: String,
    val path: String,

    val nameHash: Int = teacherNameHash(name),
)


fun teacherNameHash(name: String): Int {
    return name.trim().split(" ").sorted().joinToString(" ").lowercase().replace("ё", "е").hashCode()
}