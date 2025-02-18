package ru.lavafrai.maiapp.data.repositories

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import ru.lavafrai.maiapp.JsonProvider
import ru.lavafrai.maiapp.models.annotations.LessonAnnotation
import ru.lavafrai.maiapp.models.annotations.LessonAnnotationType
import ru.lavafrai.maiapp.models.annotations.isAnnotatedBy
import ru.lavafrai.maiapp.models.schedule.Lesson
import ru.lavafrai.maiapp.platform.getPlatform

object LessonAnnotationsRepository: BaseRepository() {
    // val storage = getPlatform().storage()
    private val mutex = Mutex()
    private val flows: MutableMap<String, MutableStateFlow<List<LessonAnnotation>>> = mutableMapOf()

    init {

    }

    fun follow(group: String): StateFlow<List<LessonAnnotation>> {
        return flows.getOrPut(group) {
            MutableStateFlow(loadAnnotations(group))
        }
    }

    fun followLesson(group: String, lesson: Lesson): StateFlow<List<LessonAnnotation>> {
        return follow(group).apply {
            repositoryScope.launch {
                collect {
                    flows["$group:${lesson.getUid()}"]?.value = it.filter { it.lessonUid == lesson.getUid() }
                }
            }
        }
    }

    fun buildStorageKey(group: String): String {
        return "schedule:annotations:$group"
    }

    fun loadAnnotations(group: String): List<LessonAnnotation> {
        return JsonProvider.tolerantJson.decodeFromString((storage.getStringOrNull(buildStorageKey(group)) ?: "[]"))
    }

    fun saveAnnotations(group: String, annotations: List<LessonAnnotation>) {
        storage.putString(buildStorageKey(group), JsonProvider.tolerantJson.encodeToString(annotations))
    }

    fun addAnnotation(group: String, lesson: Lesson, annotation: LessonAnnotationType, data: String? = null) {
        repositoryScope.launch {
            mutex.withLock {
                val annotations = loadAnnotations(group).toMutableList()
                annotations.add(LessonAnnotation(annotation, lesson.getUid(), data))
                saveAnnotations(group, annotations)
                flows[group]?.value = annotations
            }
        }
    }

    fun removeAnnotation(group: String, lesson: Lesson, annotation: LessonAnnotationType) {
        repositoryScope.launch {
            mutex.withLock {
                val annotations = loadAnnotations(group).toMutableList()
                annotations.removeAll { it.lessonUid == lesson.getUid() && it.type == annotation }
                saveAnnotations(group, annotations)
                flows[group]?.value = annotations
            }
        }
    }

    fun toggleAnnotation(group: String, lesson: Lesson, annotation: LessonAnnotationType, data: String? = null) {
        repositoryScope.launch {
            mutex.withLock {
                val annotations = loadAnnotations(group).toMutableList()
                if (annotations.isAnnotatedBy(lesson, annotation)) {
                    removeAnnotation(group, lesson, annotation)
                } else {
                    addAnnotation(group, lesson, annotation, data)
                }
            }
        }
    }

    fun getMarkableAnnotationTypes(): List<LessonAnnotationType> {
        return listOf(
            LessonAnnotation.FinalTest,
            LessonAnnotation.ControlWork,
            LessonAnnotation.Colloquium,
            LessonAnnotation.HomeWork,
            // LessonAnnotation.Comment,
        )
    }
}