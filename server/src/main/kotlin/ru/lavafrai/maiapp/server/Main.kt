package ru.lavafrai.maiapp.server

import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import ru.lavafrai.maiapp.server.modules.configureRouting
import ru.lavafrai.maiapp.server.modules.configureSerialization


fun main() {
    embeddedServer(
        CIO,
        port = 80,
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {
    configureRouting()
    configureSerialization()
}