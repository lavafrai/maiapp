package ru.lavafrai.maiapp.data.repositories

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import ru.lavafrai.maiapp.JsonProvider
import ru.lavafrai.maiapp.models.annotations.LessonAnnotation
import ru.lavafrai.maiapp.platform.getPlatform

object LessonAnnotationsRepository: BaseRepository() {
    val storage = getPlatform().storage()
    private val mutex = Mutex()
    private val flows: MutableMap<String, MutableStateFlow<List<LessonAnnotation>>> = mutableMapOf()

    init {

    }

    fun follow(group: String): StateFlow<List<LessonAnnotation>> {
        return flows.getOrPut(group) {
            MutableStateFlow(loadAnnotations(group))
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

    fun addAnnotation(group: String, annotation: LessonAnnotation) {
        repositoryScope.launch {
            mutex.withLock {
                val annotations = loadAnnotations(group).toMutableList()
                annotations.removeAll { it.lessonUid == annotation.lessonUid && it.type == annotation.type }
                annotations.add(annotation)
                saveAnnotations(group, annotations)
                flows[group]?.value = annotations
            }
        }
    }
}