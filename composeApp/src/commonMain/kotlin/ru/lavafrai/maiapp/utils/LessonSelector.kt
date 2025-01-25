package ru.lavafrai.maiapp.utils

import ru.lavafrai.maiapp.models.annotations.LessonAnnotation
import ru.lavafrai.maiapp.models.annotations.LessonAnnotationType
import ru.lavafrai.maiapp.models.schedule.Lesson
import ru.lavafrai.maiapp.models.schedule.LessonType
import ru.lavafrai.maiapp.models.schedule.ScheduleDay

open class LessonSelector protected constructor(
    val selector: (ScheduleDay, Lesson, List<LessonAnnotation>) -> Boolean,
) {
    fun test(day: ScheduleDay, lesson: Lesson, annotations: List<LessonAnnotation>): Boolean = selector(day, lesson, annotations)

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