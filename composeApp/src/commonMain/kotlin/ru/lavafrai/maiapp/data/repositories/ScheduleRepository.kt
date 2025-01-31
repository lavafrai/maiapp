@file:OptIn(ExperimentalSerializationApi::class)

package ru.lavafrai.maiapp.data.repositories

import io.ktor.client.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.lavafrai.maiapp.BuildConfig.API_BASE_URL
import ru.lavafrai.maiapp.models.schedule.Schedule
import ru.lavafrai.maiapp.models.schedule.ScheduleId
import ru.lavafrai.maiapp.network.MaiApi
import ru.lavafrai.maiapp.platform.getPlatform

class ScheduleRepository(
    httpClient: HttpClient = baseHttpClient,
    baseUrl: String = API_BASE_URL,
): BaseRepository() {
    private val api = MaiApi(httpClient, baseUrl)
    private val cache = getPlatform().storage()

    suspend fun getSchedule(name: ScheduleId) = withCache("schedule:${name.scheduleId}") { api.schedule(name.scheduleId) }
    suspend fun getScheduleFromCacheOrNull(name: ScheduleId) = fromCache<Schedule>("schedule:${name.scheduleId}")
    suspend fun getScheduleSizeInCache(name: ScheduleId) = cache.getStringOrNull("schedule:${name.scheduleId}")?.length?.toLong() ?: 0L

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