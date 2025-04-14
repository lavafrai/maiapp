package ru.lavafrai.maiapp.server.modules

import io.ktor.server.application.*
import io.ktor.server.routing.*
import ru.lavafrai.maiapp.network.exler.ExlerRepository
import ru.lavafrai.maiapp.network.mai.MaiRepository
import ru.lavafrai.maiapp.server.routes.*

fun Application.configureRouting(
    maiRepository: MaiRepository,
    exlerRepository: ExlerRepository,
) {
    routing {
        root()
        data()
        groups(maiRepository)
        schedule(maiRepository)
        teachers(maiRepository)
        exler(exlerRepository)
    }
}
