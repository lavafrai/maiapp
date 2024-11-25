package ru.lavafrai.maiapp.data.repositories

import io.ktor.client.*
import ru.lavafrai.maiapp.network.MaiApi

class ScheduleRepository(
    httpClient: HttpClient,
    baseUrl: String
) {
    private val api = MaiApi(httpClient, baseUrl)

    suspend fun getSchedule(name: String) = api.schedule(name)
}