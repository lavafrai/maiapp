package ru.lavafrai.maiapp.models.annotations

import kotlinx.serialization.Serializable
import ru.lavafrai.maiapp.models.schedule.LessonLike
import ru.lavafrai.maiapp.models.schedule.matchesUid

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

fun List<LessonAnnotation>.toggle(lesson: LessonLike, type: LessonAnnotationType): List<LessonAnnotation> {
    return if (any {lesson.matchesUid(it.lessonUid) && it.type == type}) {
        val newList = toMutableList()
        newList.filter { !lesson.matchesUid(it.lessonUid) || it.type != type }
    } else {
        val newList = toMutableList()
        newList.add(LessonAnnotation(type, lesson.getUid()))
        newList
    }
}

fun List<LessonAnnotation>.getAnnotationOrNull(lesson: LessonLike, type: LessonAnnotationType): LessonAnnotation? {
    return find { lesson.matchesUid(it.lessonUid) && it.type == type }
}

fun List<LessonAnnotation>.setAnnotationData(lesson: LessonLike, type: LessonAnnotationType, data: String): List<LessonAnnotation> {
    val newList = toMutableList().map {
        if (lesson.matchesUid(it.lessonUid) && it.type == type) it.copy(data = data)
        else it
    }
    return newList
}

fun List<LessonAnnotation>.isAnnotatedBy(lesson: LessonLike, type: LessonAnnotationType): Boolean {
    return any {lesson.matchesUid(it.lessonUid) && it.type == type}
}
