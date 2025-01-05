package ru.lavafrai.maiapp.server.routes

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ru.lavafrai.maiapp.network.exler.ExlerRepository
import ru.lavafrai.maiapp.utils.mapAsync

fun Route.exler(
    exlerRepository: ExlerRepository,
) {
    get("/exler-teachers") {
        val exlerTeachers = exlerRepository.getExlerTeachers()
        call.respond(exlerTeachers)
    }

    get("/exler-teacher/{id}") {
        val id = call.parameters["id"]!!
        val teacherId = exlerRepository.getExlerTeacherByName(id) ?: run {
            call.respondText("Teacher not found", status = HttpStatusCode.NotFound)
            return@get
        }
        val teacherInfo = exlerRepository.getExlerTeacherReviews(teacherId) ?: run {
            call.respondText("Teacher reviews not found", status = HttpStatusCode.NotFound)
            return@get
        }

        call.respond(teacherInfo) /* TODO fetch teacher by name */
    }
}