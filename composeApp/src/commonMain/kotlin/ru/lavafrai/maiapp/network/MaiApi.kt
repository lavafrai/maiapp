package ru.lavafrai.maiapp.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import net.thauvin.erik.urlencoder.UrlEncoderUtil
import ru.lavafrai.maiapp.models.exler.ExlerTeacher
import ru.lavafrai.maiapp.models.exler.ExlerTeacherInfo
import ru.lavafrai.maiapp.models.exceptions.MaiAppException
import ru.lavafrai.maiapp.models.group.Group
import ru.lavafrai.maiapp.models.maidata.MaiDataManifest
import ru.lavafrai.maiapp.models.schedule.Schedule
import ru.lavafrai.maiapp.models.schedule.TeacherId

class MaiApi(
    val httpClient: HttpClient,
    val baseUrl: String,
) {
    val urlencoder = UrlEncoderUtil

    suspend fun groups(): List<Group> = get<List<Group>> {
        url("$baseUrl/groups")
    }.filter { it.name.isNotBlank() }

    suspend fun teachers(): List<TeacherId> = get<List<TeacherId>> {
        url("$baseUrl/teachers")
    }.filter { it.name.isNotBlank() }

    suspend fun schedule(name: String): Schedule = get {
        val groupName = urlencoder.encode(name)
        url("$baseUrl/schedule/$groupName")
    }

    suspend fun exlerTeachers(): List<ExlerTeacher> = get {
        url("$baseUrl/exler-teachers")
    }

    suspend fun exlerTeacherInfo(teacherId: String): ExlerTeacherInfo = get {
        val teacherIdEncoded = urlencoder.encode(teacherId)
        url("$baseUrl/exler-teacher/$teacherIdEncoded")
    }

    suspend fun data() = get<MaiDataManifest> {
        url("$baseUrl/data")
    }

    suspend fun asset(path: String) = get<Any> {
        val pathEncoded = urlencoder.encode(path)
        url("$baseUrl/asset/$pathEncoded")
    }

    private suspend inline fun <reified T> get(
        crossinline builder: HttpRequestBuilder.() -> Unit,
    ): T {
        val response = httpClient.get { builder() }
        if (!response.status.isSuccess()) {
            val message = response.headers["X-Error-Message"]
                ?: response.bodyAsText().takeIf { it.isNotBlank() }
                ?: "API request failed: ${response.status}"
            throw MaiApiException(message, response.status.value)
        }
        return response.body()
    }
}

class MaiApiException(
    message: String,
    val statusCode: Int,
): MaiAppException(message) {
    override fun getReadableDescription(): String = message ?: "Ошибка API ($statusCode)"
}
