package ru.lavafrai.maiapp.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import net.thauvin.erik.urlencoder.UrlEncoderUtil
import ru.lavafrai.maiapp.models.exler.ExlerTeacher
import ru.lavafrai.maiapp.models.exler.ExlerTeacherInfo
import ru.lavafrai.maiapp.models.group.Group
import ru.lavafrai.maiapp.models.maidata.MaiDataManifest
import ru.lavafrai.maiapp.models.schedule.Schedule
import ru.lavafrai.maiapp.models.schedule.TeacherId

class MaiApi(
    val httpClient: HttpClient,
    val baseUrl: String,
) {
    val urlencoder = UrlEncoderUtil

    suspend fun groups(): List<Group> = httpClient.get {
        url("$baseUrl/groups")
    }.body<List<Group>>().filter { it.name.isNotBlank() }

    suspend fun teachers(): List<TeacherId> = httpClient.get {
        url("$baseUrl/teachers")
    }.body<List<TeacherId>>().filter { it.name.isNotBlank() }

    suspend fun schedule(name: String): Schedule = httpClient.get {
        val groupName = urlencoder.encode(name)
        url("$baseUrl/schedule/$groupName")
    }.body<Schedule>()

    suspend fun exlerTeachers(): List<ExlerTeacher> = httpClient.get {
        url("$baseUrl/exler-teachers")
    }.body<List<ExlerTeacher>>()

    suspend fun exlerTeacherInfo(teacherId: String): ExlerTeacherInfo = httpClient.get {
        val teacherIdEncoded = urlencoder.encode(teacherId)
        url("$baseUrl/exler-teacher/$teacherIdEncoded")
    }.body<ExlerTeacherInfo>()

    suspend fun data() = httpClient.get {
        url("$baseUrl/data")
    }.body<MaiDataManifest>()

    suspend fun asset(path: String) = httpClient.get {
        val pathEncoded = urlencoder.encode(path)
        url("$baseUrl/asset/$pathEncoded")
    }.body<Any>()
}