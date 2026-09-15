package ru.lavafrai.maiapp

import io.ktor.client.*
import io.ktor.client.plugins.api.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*

// Keep in sync with Android Network Security Config and iOS Info.plist.
internal fun isMyMaiHost(host: String): Boolean =
    host.lowercase().removeSuffix(".") in setOf("esia.mai.ru", "my.mai.ru")

private val MyMaiHttpsOnly = createClientPlugin("MyMaiHttpsOnly") {
    onRequest { request, _ ->
        require(request.url.protocol == URLProtocol.HTTPS) { "My MAI requests require HTTPS" }
    }
}

internal fun HttpClientConfig<*>.configureMyMaiClient() {
    install(MyMaiHttpsOnly)
    // Ktor also rejects HTTPS -> HTTP redirects by default.
    install(ContentNegotiation) {
        json(JsonProvider.tolerantJson)
    }
}
