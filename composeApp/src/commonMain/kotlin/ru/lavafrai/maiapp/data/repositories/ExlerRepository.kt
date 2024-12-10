@file:OptIn(ExperimentalSerializationApi::class)

package ru.lavafrai.maiapp.data.repositories

import com.russhwolf.settings.set
import io.ktor.client.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.lavafrai.maiapp.models.exler.ExlerTeacher
import ru.lavafrai.maiapp.network.MaiApi
import ru.lavafrai.maiapp.platform.getPlatform

class ExlerRepository(
    httpClient: HttpClient = baseHttpClient,
    baseUrl: String = "https://mai-exler.ru",
): BaseRepository() {
    private val api = MaiApi(httpClient, baseUrl)
    private val cache = getPlatform().storage()

    suspend fun getTeachers() = withCache("exler-teachers") { api.exlerTeachers() }
    fun getTeachersFromCacheOrNull() = fromCache<List<ExlerTeacher>>("exler-teachers")
    suspend fun getTeacherInfo(teacherId: String) = api.exlerTeacherInfo(teacherId)

    private suspend inline fun <reified T>withCache(key: String, block: () -> @Serializable T): T {
        val data = block()
        val encoded = Json.encodeToString(data)
        cache.set(key, encoded)
        return data
    }

    private inline fun <reified T> fromCache(key: String): T? {
        val encoded = cache.getStringOrNull(key)
        val data = if (encoded != null) try {
            Json.decodeFromString<T>(encoded)
        } catch (e: Exception) {
            null
        } else null

        return data
    }
}