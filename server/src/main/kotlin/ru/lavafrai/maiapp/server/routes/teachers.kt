package ru.lavafrai.maiapp.server.routes

import io.ktor.server.routing.*
import io.ktor.server.response.*
import ru.lavafrai.maiapp.network.mai.MaiRepository

fun Route.teachers(
    maiRepository: MaiRepository,
) {
    get("/teachers") {
        val teachers = maiRepository.getTeachers()
        call.respond(teachers)
    }
}