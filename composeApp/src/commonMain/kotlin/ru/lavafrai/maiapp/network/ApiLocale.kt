package ru.lavafrai.maiapp.network

import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.http.Url
import ru.lavafrai.maiapp.BuildConfig
import ru.lavafrai.maiapp.localizers.appLocaleTag

private val apiHost = Url(BuildConfig.API_BASE_URL).host

/** Adds the app locale to every request to our server (not to other sites), so that it can answer in that language */
val ApiLocale = createClientPlugin("ApiLocale") {
    onRequest { request, _ ->
        if (request.url.host == apiHost && !request.url.parameters.contains("locale")) {
            request.url.parameters.append("locale", appLocaleTag())
        }
    }
}
