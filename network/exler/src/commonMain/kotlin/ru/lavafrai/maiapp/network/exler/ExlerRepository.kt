package ru.lavafrai.maiapp.network.exler

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import ru.lavafrai.maiapp.HttpClientProvider
import ru.lavafrai.maiapp.network.exler.parser.parseTeacherList

class ExlerRepository(
    private val httpClient: HttpClient = HttpClientProvider.default,
) {
    suspend fun httpGet(url: String) = httpClient.get(url).bodyAsText()

    suspend fun getExlerTeachers() = parseTeacherList(this::httpGet)
}