package ru.lavafrai.maiapp.models.exler

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.lavafrai.maiapp.models.Nameable

@Serializable
data class ExlerTeacher(
    @SerialName("name") private val internalName: String,
    val path: String,
    val faculty: ExlerFaculty,

    val nameHash: Int = teacherNameHash(internalName),
): Nameable {
    override val name: String
        get() = internalName
}


fun teacherNameHash(name: String): Int {
    return name.trim().split(" ").sorted().joinToString(" ").lowercase().replace("ё", "е").hashCode()
}