package ru.lavafrai.maiapp.server.routes

import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ru.lavafrai.maiapp.models.maidata.MaiDataItem
import ru.lavafrai.maiapp.models.maidata.MaiDataItemType
import ru.lavafrai.maiapp.models.maidata.MaiDataManifest
import ru.lavafrai.maiapp.models.maidata.asset.Asset

fun Route.data() {
    staticResources(
        "assets",
        "data/assets",
    )

    get("/data") {
        val manifest = MaiDataManifest(
            version = 1,
            data = buildList {
                testData()
                campusData()
                teachersData()
                lifestyleData()
            },
        )

        call.respond(manifest)
    }
}

fun MutableList<MaiDataItem>.testData() {
    add(
        MaiDataItem(
            type = MaiDataItemType.DoNothing,
            accent = true,
            name = "Тестовый информационный баннер",
        )
    )
}

fun MutableList<MaiDataItem>.teachersData() {
    add(
        MaiDataItem(
            type = MaiDataItemType.Builtin,
            name = "Расписания преподавателей",
            category = "Преподаватели",
            icon = Asset.relative("/assets/icons/users.svg"),
            asset = Asset.text("teacher-schedule")
        )
    )

    add(
        MaiDataItem(
            type = MaiDataItemType.Builtin,
            name = "О преподавателях",
            category = "Преподаватели",
            icon = Asset.relative("/assets/icons/message-square.svg"),
            asset = Asset.text("teacher-review")
        )
    )
}

fun MutableList<MaiDataItem>.campusData() {
    add(
        MaiDataItem(
            type = MaiDataItemType.Map,
            name = "План студгородка",
            category = "Студгородок",
            icon = Asset.relative("/assets/icons/map.svg"),
            asset = Asset.webview("images/map.png"),
            assetNight = Asset.webview("images/map-night.png")
        )
    )
    add(
        MaiDataItem(
            type = MaiDataItemType.Web,
            name = "Столовые и буфеты",
            category = "Студгородок",
            icon = Asset.relative("/assets/icons/coffee.svg"),
            asset = Asset.webview("markup/cafeterias.html")
        )
    )
    add(
        MaiDataItem(
            type = MaiDataItemType.Web,
            name = "Библиотеки",
            category = "Студгородок",
            icon = Asset.relative("/assets/icons/book-open.svg"),
            asset = Asset.webview("markup/libraries.html")
        )
    )
}

fun MutableList<MaiDataItem>.lifestyleData() {
    add(
        MaiDataItem(
            type = MaiDataItemType.Web,
            name = "Спортивные секции",
            category = "Жизнь",
            icon = Asset.relative("/assets/icons/biking-solid.svg"),
            asset = Asset.webview("")
        )
    )

    add(
        MaiDataItem(
            type = MaiDataItemType.Web,
            name = "Маёвский словарик",
            category = "Жизнь",
            icon = Asset.relative("/assets/icons/book-solid.svg"),
            asset = Asset.webview("")
        )
    )

    add(
        MaiDataItem(
            type = MaiDataItemType.Web,
            name = "Творческие коллективы",
            category = "Жизнь",
            icon = Asset.relative("/assets/icons/palette.svg"),
            asset = Asset.webview("")
        )
    )

    add(
        MaiDataItem(
            type = MaiDataItemType.Web,
            name = "Студенческие организации",
            category = "Жизнь",
            icon = Asset.relative("/assets/icons/user-friends.svg"),
            asset = Asset.webview("")
        )
    )
}