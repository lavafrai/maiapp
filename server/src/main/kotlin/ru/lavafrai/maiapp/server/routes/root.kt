package ru.lavafrai.maiapp.server.routes

import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.root() {
    get("/") {
        call.respond("Hello, world!\nIt's MAI app server!")
    }
}