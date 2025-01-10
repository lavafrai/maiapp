package ru.lavafrai.maiapp.utils

import io.ktor.http.*

fun globalUrl(baseUrl: String, path: String): String {
    if (!path.startsWith("/")) {
        return "$baseUrl${if (baseUrl.endsWith("/")) "" else "/"}$path"
    } else {
        val host = Url(baseUrl).host
        val protocol = Url(baseUrl).protocol
        val port = Url(baseUrl).port
        return "${protocol.name}://$host.${if (protocol.defaultPort == port) "" else ":$port"}$path"
    }
}