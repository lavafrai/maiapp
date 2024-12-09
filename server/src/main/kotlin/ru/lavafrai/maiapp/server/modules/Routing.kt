package ru.lavafrai.maiapp.server.modules

import io.ktor.server.application.*
import io.ktor.server.routing.*
import ru.lavafrai.maiapp.server.routes.root

fun Application.configureRouting() {
    routing {
        root()
    }
}