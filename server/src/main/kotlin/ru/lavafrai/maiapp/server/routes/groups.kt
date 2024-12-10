package ru.lavafrai.maiapp.server.routes

import io.ktor.server.response.*
import io.ktor.server.routing.*
import mai.MaiRepository

fun Route.groups(
    maiRepository: MaiRepository,
) {
    get("/groups") {
        val groups = maiRepository.getGroups()
        call.respond(groups)
    }
}