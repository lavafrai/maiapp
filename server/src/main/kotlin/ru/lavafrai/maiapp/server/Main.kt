package ru.lavafrai.maiapp.server

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import mai.MaiRepository
import ru.lavafrai.maiapp.server.modules.configureCaching
import ru.lavafrai.maiapp.server.modules.configureRouting
import ru.lavafrai.maiapp.server.modules.configureSerialization


fun main() {
    embeddedServer(
        Netty,
        port = 80,
        host = "0.0.0.0",
        module = Application::module,
    ).start(true)
}

fun Application.module() {
    val maiRepository = MaiRepository()

    configureRouting(
        maiRepository = maiRepository,
    )
    configureSerialization()
    configureCaching()
}