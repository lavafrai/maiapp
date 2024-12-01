package ru.lavafrai.maiapp.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import ru.lavafrai.maiapp.models.exler.ExlerTeacher
import ru.lavafrai.maiapp.models.exler.ExlerTeacherInfo
import ru.lavafrai.maiapp.models.group.Group
import ru.lavafrai.maiapp.models.schedule.Schedule
import ru.lavafrai.maiapp.models.schedule.TeacherId

class MaiApi(
    val httpClient: HttpClient,
    val baseUrl: String,
) {
    suspend fun groups(): List<Group> = httpClient.get {
        url("$baseUrl/api/v1/groups")
    }.body<List<Group>>().filter { it.name.isNotBlank() }

    suspend fun teachers(): List<TeacherId> = httpClient.get {
        url("$baseUrl/api/v1/teachers")
    }.body<List<TeacherId>>().filter { it.name.isNotBlank() }

    suspend fun schedule(name: String): Schedule = httpClient.get {
        url("$baseUrl/api/v1/schedule/$name")
    }.body<Schedule>()

    suspend fun exlerTeachers(): List<ExlerTeacher> = httpClient.get {
        url("$baseUrl/api/v1/exler-teachers")
    }.body<List<ExlerTeacher>>()

    suspend fun exlerTeacherInfo(teacherId: String): ExlerTeacherInfo = httpClient.get {
        url("$baseUrl/api/v1/exler-teacher/$teacherId")
    }.body<ExlerTeacherInfo>()
}