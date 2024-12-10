package ru.lavafrai.maiapp.server.routes

import io.ktor.server.routing.*
import io.ktor.server.response.*
import mai.MaiRepository

fun Route.schedule(
    maiRepository: MaiRepository,
) {
    get("/schedule/{group}") {
        val group = call.parameters["group"] ?: return@get

        val schedule = maiRepository.getGroupSchedule(group)
        call.respond(schedule)
    }
}