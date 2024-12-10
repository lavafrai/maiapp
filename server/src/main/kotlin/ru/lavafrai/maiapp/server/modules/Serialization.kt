package ru.lavafrai.maiapp.server.modules

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import ru.lavafrai.maiapp.JsonProvider

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(json = JsonProvider.tolerantJson)
    }
}