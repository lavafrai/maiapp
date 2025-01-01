package ru.lavafrai.maiapp.models.annotations

import kotlinx.serialization.Serializable
import ru.lavafrai.maiapp.models.schedule.Lesson

@Serializable
data class LessonAnnotation(
    val type: LessonAnnotationType,
    val lessonUid: Int,
    val data: String? = ""
) {
    companion object {
        val FinalTest = LessonAnnotationType("Зачет", 15)
        val ControlWork = LessonAnnotationType("Control Work", 10)
        val Colloquium = LessonAnnotationType("Colloquium", 9)
        val HomeWork = LessonAnnotationType("Home Work", 1, hasUserData = true)
        val Comment = LessonAnnotationType("Comment", 0, hasUserData = true)
        val Skipped = LessonAnnotationType("Skipped", 1)
    }
}

fun List<LessonAnnotation>.toggle(lesson: Lesson, type: LessonAnnotationType): List<LessonAnnotation> {
    return if (any {it.lessonUid == lesson.getUid() && it.type == type}) {
        val newList = toMutableList()
        newList.filter { it.lessonUid != lesson.getUid() || it.type != type }
    } else {
        val newList = toMutableList()
        newList.add(LessonAnnotation(type, lesson.getUid()))
        newList
    }
}

fun List<LessonAnnotation>.getAnnotationOrNull(lesson: Lesson, type: LessonAnnotationType): LessonAnnotation? {
    return find { it.lessonUid == lesson.getUid() && it.type == type }
}

fun List<LessonAnnotation>.setAnnotationData(lesson: Lesson, type: LessonAnnotationType, data: String): List<LessonAnnotation> {
    val newList = toMutableList().map {
        if (it.lessonUid == lesson.getUid() && it.type == type) it.copy(data = data)
        else it
    }
    return newList
}

fun List<LessonAnnotation>.isAnnotatedBy(lesson: Lesson, type: LessonAnnotationType): Boolean {
    return any {it.lessonUid == lesson.getUid() && it.type == type}
}
