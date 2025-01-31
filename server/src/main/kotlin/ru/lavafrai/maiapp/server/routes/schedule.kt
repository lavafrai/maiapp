package ru.lavafrai.maiapp.server.routes

import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import ru.lavafrai.maiapp.exceptions.InvalidInlineClassValue
import ru.lavafrai.maiapp.exceptions.NotFoundException
import ru.lavafrai.maiapp.models.schedule.GroupName
import ru.lavafrai.maiapp.models.schedule.TeacherName
import ru.lavafrai.maiapp.models.schedule.TeacherUid
import ru.lavafrai.maiapp.network.mai.MaiRepository

fun Route.schedule(
    maiRepository: MaiRepository,
) {
    get("/schedule/{group}") {
        val group = call.parameters["group"] ?: return@get

        /*val validators = listOf(
            TeacherUid.isValid(group),
            GroupName.isValid(group),
            TeacherName.isValid(group),
        )
        if (!validators.any { it }) {
            call.respondText("schedule($group) not found, invalid format", status = HttpStatusCode.NotFound)
            return@get
        }*/

        val schedule = try {
            when {
                TeacherUid.isValid(group) -> maiRepository.getTeacherSchedule(TeacherUid(group))
                GroupName.isValid(group) -> maiRepository.getGroupSchedule(GroupName(group))
                TeacherName.isValid(group) -> {
                    val teachers = maiRepository.getTeachers()
                    val teacher = teachers.find { it.name == group }
                    if (teacher == null) {
                        call.respondText("schedule($group) not found, teacher id is currently unknown", status = HttpStatusCode.NotFound)
                        return@get
                    }
                    maiRepository.getTeacherSchedule(teacher.uid)
                }
                else -> throw InvalidInlineClassValue()
            }
        } catch (e: NotFoundException) {
            call.respondText("schedule($group) not found", status = HttpStatusCode.NotFound)
            return@get
        } catch (e: InvalidInlineClassValue) {
            call.respondText("schedule($group) not found, invalid format", status = HttpStatusCode.NotFound)
            return@get
        }

        call.respond(schedule)
    }
}