@file:OptIn(ExperimentalSerializationApi::class)

package ru.lavafrai.maiapp.data.repositories

import io.ktor.client.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.lavafrai.maiapp.models.schedule.Schedule
import ru.lavafrai.maiapp.network.MaiApi
import ru.lavafrai.maiapp.platform.getPlatform

class ScheduleRepository(
    httpClient: HttpClient = baseHttpClient,
    baseUrl: String,
): BaseRepository() {
    private val api = MaiApi(httpClient, baseUrl)
    private val cache = getPlatform().storage()

    suspend fun getSchedule(name: String) = withCache("schedule:$name") { api.schedule(name) }
    suspend fun getScheduleFromCacheOrNull(name: String) = fromCache<Schedule>("schedule:$name")

    private suspend inline fun <reified T>withCache(key: String, block: () -> @Serializable T): T {
        val data = block()
        val encoded = Json.encodeToString(data)
        cache.putString(key, encoded)
        return data
    }

    private suspend inline fun <reified T> fromCache(key: String): T? {
        val encoded = cache.getStringOrNull(key)
        val data = if (encoded != null) try {
            Json.decodeFromString<T>(encoded)
        } catch (e: Exception) {
            null
        } else null

        return data
    }
}