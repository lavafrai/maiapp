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
                icon = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"24\" height=\"24\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\" class=\"feather feather-at-sign\"><circle cx=\"12\" cy=\"12\" r=\"4\"></circle><path d=\"M16 8v5a3 3 0 0 0 6 0v-1a10 10 0 1 0-3.92 7.94\"></path></svg>"
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