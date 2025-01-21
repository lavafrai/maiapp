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
                icon = "PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIyNCIgaGVpZ2h0PSIyNCIgdmlld0JveD0iMCAwIDI0IDI0IiBmaWxsPSJub25lIiBzdHJva2U9ImN1cnJlbnRDb2xvciIgc3Ryb2tlLXdpZHRoPSIyIiBzdHJva2UtbGluZWNhcD0icm91bmQiIHN0cm9rZS1saW5lam9pbj0icm91bmQiIGNsYXNzPSJmZWF0aGVyIGZlYXRoZXItYXQtc2lnbiI+PGNpcmNsZSBjeD0iMTIiIGN5PSIxMiIgcj0iNCI+PC9jaXJjbGU+PHBhdGggZD0iTTE2IDh2NWEzIDMgMCAwIDAgNiAwdi0xYTEwIDEwIDAgMSAwLTMuOTIgNy45NCI+PC9wYXRoPjwvc3ZnPg==".toByteArray()
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