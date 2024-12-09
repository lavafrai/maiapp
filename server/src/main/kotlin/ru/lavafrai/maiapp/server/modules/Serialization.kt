package ru.lavafrai.maiapp.server.modules

import JsonProvider
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(json = JsonProvider.tolerantJson)
    }
}