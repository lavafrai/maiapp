@file:OptIn(ExperimentalSerializationApi::class)

package ru.lavafrai.maiapp.data.repositories

import com.russhwolf.settings.set
import io.ktor.client.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.lavafrai.maiapp.models.schedule.Schedule
import ru.lavafrai.maiapp.network.MaiApi
import ru.lavafrai.maiapp.platform.getPlatformSettingsStorage

class ScheduleRepository(
    httpClient: HttpClient,
    baseUrl: String
) {
    private val api = MaiApi(httpClient, baseUrl)
    private val cache = getPlatformSettingsStorage()

    suspend fun getSchedule(name: String) = withCache("schedule:$name") { api.schedule(name) }
    suspend fun getScheduleFromCacheOrNull(name: String) = fromCache<Schedule>("schedule:$name")

    suspend fun withCache(key: String, block: suspend () -> Schedule): Schedule {
        val data = block()
        val encoded = Json.encodeToString(data)
        cache.set(key, encoded)
        return data
    }

    private suspend inline fun <reified T> fromCache(key: String): T? {
        val encoded = cache.getStringOrNull(key)
        val data = if (encoded != null) try {
            Json.decodeFromString<T>(encoded!!)
        } catch (e: Exception) {
            null
        } else null

        return data
    }
}