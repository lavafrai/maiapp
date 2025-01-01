package ru.lavafrai.maiapp.server.modules

import io.ktor.server.application.*
import io.ktor.server.routing.*
import ru.lavafrai.maiapp.network.exler.ExlerRepository
import ru.lavafrai.maiapp.network.mai.MaiRepository
import ru.lavafrai.maiapp.server.routes.exler
import ru.lavafrai.maiapp.server.routes.groups
import ru.lavafrai.maiapp.server.routes.root
import ru.lavafrai.maiapp.server.routes.schedule

fun Application.configureRouting(
    maiRepository: MaiRepository,
    exlerRepository: ExlerRepository,
) {
    routing {
        root()
        groups(maiRepository)
        schedule(maiRepository)
        exler(exlerRepository)
    }
}