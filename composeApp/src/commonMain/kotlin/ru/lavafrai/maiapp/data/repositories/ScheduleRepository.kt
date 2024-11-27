@file:OptIn(ExperimentalSerializationApi::class)

package ru.lavafrai.maiapp.data.repositories

import com.russhwolf.settings.serialization.decodeValueOrNull
import com.russhwolf.settings.serialization.encodeValue
import io.ktor.client.*
import kotlinx.serialization.ExperimentalSerializationApi
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
    suspend fun getScheduleFromCacheOrNull(name: String) = cache.decodeValueOrNull<Schedule>("schedule:$name")

    suspend fun withCache(key: String, block: suspend () -> Schedule): Schedule {
        val data = block()
        cache.encodeValue(key, data)
        return data
    }
}