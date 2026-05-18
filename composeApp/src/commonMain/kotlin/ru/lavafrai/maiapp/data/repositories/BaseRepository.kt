package ru.lavafrai.maiapp.data.repositories

import com.russhwolf.settings.Settings
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json
import ru.lavafrai.maiapp.JsonProvider
import ru.lavafrai.maiapp.platform.getPlatform

open class BaseRepository {
    private val dispatchers = getPlatform().dispatchers()
    protected val repositoryScope = CoroutineScope(dispatchers.IO)
    protected val storage: Settings = getPlatform().storage()
    protected val json = JsonProvider.tolerantJson

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