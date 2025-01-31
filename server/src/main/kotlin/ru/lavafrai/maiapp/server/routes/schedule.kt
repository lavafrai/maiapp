package ru.lavafrai.maiapp.server.routes

import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import ru.lavafrai.maiapp.exceptions.NotFoundException
import ru.lavafrai.maiapp.network.mai.MaiRepository

fun Route.schedule(
    maiRepository: MaiRepository,
) {
    get("/schedule/{group}") {
        val group = call.parameters["group"] ?: return@get

        val schedule = try {
            maiRepository.getGroupSchedule(group)
        } catch (e: NotFoundException) { null } ?: try {
            maiRepository.getTeacherSchedule(group)
        } catch (e: NotFoundException) {
            call.respondText("schedule($group) not found", status = HttpStatusCode.NotFound)
            return@get
        }

        call.respond(schedule)
    }
}