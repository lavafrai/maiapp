package ru.lavafrai.maiapp.data.repositories

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import ru.lavafrai.maiapp.platform.getPlatform

open class BaseRepository {
    companion object {
        val baseHttpClient = HttpClient(getPlatform().ktorEngine()) {
            followRedirects = true

            install(ContentNegotiation) {
                json(
                    json = Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                    }
                )
            }
        }
    }
}