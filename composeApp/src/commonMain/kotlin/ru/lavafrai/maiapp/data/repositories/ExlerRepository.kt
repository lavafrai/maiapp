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

class ExlerRepository(
    httpClient: HttpClient,
    baseUrl: String
) {
    private val api = MaiApi(httpClient, baseUrl)
    private val cache = getPlatformSettingsStorage()

    suspend fun getTeachers() = api.exlerTeachers()
    suspend fun getTeacherInfo(teacherId: String) = api.exlerTeacherInfo(teacherId)

    suspend fun withCache(key: String, block: suspend () -> Schedule): Schedule {
        val data = block()
        val encoded = Json.encodeToString(data)
        cache.set(key, encoded)
        return data
    }
}