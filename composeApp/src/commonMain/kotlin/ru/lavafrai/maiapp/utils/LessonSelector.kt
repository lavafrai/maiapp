package ru.lavafrai.maiapp.utils

import kotlinx.datetime.LocalDate
import ru.lavafrai.maiapp.models.annotations.LessonAnnotation
import ru.lavafrai.maiapp.models.annotations.LessonAnnotationType
import ru.lavafrai.maiapp.models.schedule.LessonLike
import ru.lavafrai.maiapp.models.schedule.LessonType

open class LessonSelector protected constructor(
    val selector: (LocalDate, LessonLike, List<LessonAnnotation>) -> Boolean,
) {
    fun test(day: LocalDate, lesson: LessonLike, annotations: List<LessonAnnotation>): Boolean = selector(day, lesson, annotations)

    override fun hashCode(): Int {
        return selector.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as LessonSelector

        return selector == other.selector
    }

    companion object {
        fun default(): LessonSelector = object : LessonSelector({ _, _, _ -> true }) {
            override fun hashCode(): Int {
                return 0
            }
        }

        fun militaryHideDefault(): LessonSelector = object : LessonSelector({ day, lesson, _ ->
            lesson.name != "Военная подготовка"
        }) {
            override fun hashCode(): Int {
                return "military".hashCode()
            }
        }

        fun type(lessonType: LessonType): LessonSelector = object : LessonSelector({ _, lesson, _ -> lesson.type == lessonType }) {
            override fun hashCode(): Int {
                return lessonType.hashCode()
            }
        }

        fun annotation(annotation: LessonAnnotationType): LessonSelector = object : LessonSelector({ _, _, annotations -> annotations.any { it.type == annotation } }) {
            override fun hashCode(): Int {
                return annotation.hashCode()
            }
        }
    }
}

fun List<LessonSelector>.anySelector() = object : LessonSelector({ day, lesson, annotations -> this@anySelector.any { it.test(day, lesson, annotations) } }) {
    override fun hashCode(): Int {
        return this@anySelector.fold(0) { acc, selector -> acc + selector.hashCode() * 31 }
    }
}