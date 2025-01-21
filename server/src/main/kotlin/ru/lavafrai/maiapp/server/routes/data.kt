package ru.lavafrai.maiapp.server.routes

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ru.lavafrai.maiapp.models.maidata.MaiDataItem
import ru.lavafrai.maiapp.models.maidata.MaiDataItemType
import ru.lavafrai.maiapp.models.maidata.MaiDataManifest

fun Route.data() {
    val manifest = MaiDataManifest(
        version = 1,
        data = listOf(
            MaiDataItem(
                type = MaiDataItemType.Builtin,
                name = "О преподавателях",
                forTeachers = false,
                category = "Преподаватели",
            ),
            MaiDataItem(
                type = MaiDataItemType.Builtin,
                name = "Расписание преподавателей",
                category = "Преподаватели",
            ),
            MaiDataItem(
                type = MaiDataItemType.Map,
                name = "План студгородка",
                category = "Студгородок",
                asset = "map/campus.png",
                assetNight = "map/campus-night.png",
            ),
        ),
    )

    get("/data") {
        call.respond(manifest.data)
    }

    get("/asset/{asset}") {
        val asset = call.parameters["asset"] ?: return@get call.respondText("Asset not found", status = HttpStatusCode.NotFound)
        // call.respondFile("assets/$asset")
    }
}