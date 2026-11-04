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

/**
 * Приводит ФИО к виду, пригодному для сравнения: без учёта регистра, ё/е и порядка слов.
 *
 * В расписании МАИ и в отзывах СтЭна одно и то же имя может быть записано по-разному
 * («Пётр» и «Петр», «Иванов Иван» и «Иван Иванов»), поэтому строки нельзя сравнивать напрямую.
 */
fun normalizeTeacherName(name: String): String {
    return name
        .trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .sorted()
        .joinToString(" ")
        .lowercase()
        .replace("ё", "е")
}

fun teacherNameHash(name: String): Int {
    return normalizeTeacherName(name).hashCode()
}

/** Преподаватель из списка СтЭна с тем же ФИО, что и [name], или null, если его там нет */
fun List<ExlerTeacher>.findByTeacherName(name: String): ExlerTeacher? {
    val normalizedName = normalizeTeacherName(name)
    return find { normalizeTeacherName(it.name) == normalizedName }
}
