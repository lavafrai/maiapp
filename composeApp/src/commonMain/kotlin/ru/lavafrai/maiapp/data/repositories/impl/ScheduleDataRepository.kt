package ru.lavafrai.maiapp.data.repositories.impl

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import ru.lavafrai.maiapp.platform.getPlatformKtorEngine
import ru.lavafrai.maiapp.data.repositories.ScheduleDataRepository


class ScheduleDataRepositoryImpl : ScheduleDataRepository {
    val httpClient = HttpClient(getPlatformKtorEngine()) {
        install(ContentNegotiation) {
            json()
        }
    }

    /*override suspend fun getGroups(): List<Group> {
        TODO("Not yet implemented")
    }*/
}