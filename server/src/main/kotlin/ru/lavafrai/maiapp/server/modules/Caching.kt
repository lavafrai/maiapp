package ru.lavafrai.maiapp.server.modules

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cachingheaders.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.util.*
import ru.lavafrai.maiapp.Constants

fun Application.configureCaching() {
    install(CachingHeaders) {
        options { call, content ->
            val path = Url(call.url()).fullPath
            when {
                path.startsWith("/schedule") ->
                    CachingOptions(CacheControl.MaxAge(
                        maxAgeSeconds = Constants.SCHEDULE_CACHE_TIME,
                        proxyMaxAgeSeconds = Constants.SCHEDULE_CACHE_TIME,
                        mustRevalidate = false,
                        proxyRevalidate = false,
                    ))
                path.startsWith("/groups") -> CachingOptions(CacheControl.MaxAge(
                    maxAgeSeconds = Constants.GROUPS_CACHE_TIME,
                    proxyMaxAgeSeconds = Constants.GROUPS_CACHE_TIME,
                    mustRevalidate = false,
                    proxyRevalidate = false,
                ))
                else -> null
            }
        }
    }

    install(CORS) {
        allowHost("maiapp.lavafrai.ru")
        allowHost("mai3.lavafrai.ru")
        allowHeader(HttpHeaders.ContentType)
    }
}