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
 * Приводит ФИО к виду, пригодному для сравнения: без учёта регистра и ё/е.
 *
 * В расписании МАИ и в отзывах СтЭна одно и то же имя может быть записано по-разному
 * («Пётр» и «Петр», «Ёлкин» и «Елкин»). Порядок слов не нормализуем: подтверждённых
 * случаев, когда ФИО записано в другом порядке, не встречалось.
 */
fun normalizeTeacherName(name: String): String {
    return name
        .trim()
        .lowercase()
        .replace("ё", "е")
}

fun teacherNameHash(name: String): Int {
    return normalizeTeacherName(name).hashCode()
}

/**
 * Преподаватель из списка СтЭна с тем же ФИО, что и [name], или null, если его там нет.
 *
 * Точное совпадение приоритетнее: если в каталоге есть и «Ёлкин», и «Елкин»,
 * вернётся тот, что написан так же, как в расписании.
 */
fun List<ExlerTeacher>.findByTeacherName(name: String): ExlerTeacher? {
    find { it.name == name }?.let { return it }

    val normalizedName = normalizeTeacherName(name)
    return find { normalizeTeacherName(it.name) == normalizedName }
}
