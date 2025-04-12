package ru.lavafrai.maiapp.server.routes

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.http.content.*

fun Route.privacy() {
    get("/privacy") {
        val privacyPolicy = javaClass.classLoader.getResource("data/privacy_policy.txt")?.readText()
        if (privacyPolicy != null) {
            call.respondText(privacyPolicy)
        } else {
            call.respondText("Privacy policy not found", status = HttpStatusCode.NotFound)
        }
    }
}
