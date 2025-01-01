package ru.lavafrai.maiapp.server.routes

import io.ktor.server.response.*
import io.ktor.server.routing.*
import ru.lavafrai.maiapp.network.exler.ExlerRepository

fun Route.exler(
    exlerRepository: ExlerRepository,
) {
    get("/exler-teachers") {
        val exlerTeachers = exlerRepository.getExlerTeachers()
        call.respond(exlerTeachers)
    }

    get("/exler-teacher/{id}") {
        val id = call.parameters["id"]!!
        call.respond(id) /* TODO fetch teacher by name */
    }
}