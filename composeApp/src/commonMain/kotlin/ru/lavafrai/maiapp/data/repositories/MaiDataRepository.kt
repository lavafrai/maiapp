package ru.lavafrai.maiapp.data.repositories

import io.ktor.client.*
import ru.lavafrai.maiapp.BuildConfig.API_BASE_URL
import ru.lavafrai.maiapp.network.MaiApi

class MaiDataRepository(
    httpClient: HttpClient = baseHttpClient,
    baseUrl: String = API_BASE_URL,
): BaseRepository() {
    private val api = MaiApi(httpClient, baseUrl)

    suspend fun getData() = api.data()
}