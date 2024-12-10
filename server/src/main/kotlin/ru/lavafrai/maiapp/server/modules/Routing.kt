package ru.lavafrai.maiapp.server.modules

import io.ktor.server.application.*
import io.ktor.server.routing.*
import mai.MaiRepository
import ru.lavafrai.maiapp.server.routes.groups
import ru.lavafrai.maiapp.server.routes.root
import ru.lavafrai.maiapp.server.routes.schedule

fun Application.configureRouting(
    maiRepository: MaiRepository,
) {

    routing {
        root()
        groups(maiRepository)
        schedule(maiRepository)
    }
}