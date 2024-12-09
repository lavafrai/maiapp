package ru.lavafrai.maiapp.models.group

import ru.lavafrai.maiapp.models.errors.InvalidGroupNameException


class GroupNameParser(val name: String) {
    val faculty: Int
    val course: Int
    val type: EducationLevel?

    init {
        val find = ".(\\d+)(\\S?+)-(\\d+)([\\S^-]+)-(\\d+)".toRegex().find(name) ?: throw InvalidGroupNameException("Cant parse string as group name")

        faculty = find.groups[1]!!.value.toInt()
        course = find.groups[3]!!.value[0] - '0'

        val typeText = find.groups[4]!!.value
        type = when {
            typeText.startsWith("СВ") -> EducationLevel.SPECIAL_HIGH
            typeText.startsWith("БВ") -> EducationLevel.BASE_HIGH
            typeText.startsWith('С') -> EducationLevel.SPECIAL

            typeText.startsWith('Б') -> EducationLevel.BACHELOR
            typeText.startsWith('М') -> EducationLevel.MAGISTRACY
            typeText.startsWith('А') -> EducationLevel.POSTGRADUATE

            else -> null
        }
    }
}

fun Group.parseName(): GroupNameParser {
    return GroupNameParser(this.name)
}