package ru.lavafrai.maiapp.viewmodels

import androidx.lifecycle.ViewModel
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import ru.lavafrai.maiapp.BuildConfig.API_BASE_URL
import ru.lavafrai.maiapp.platform.getPlatformKtorEngine

open class MaiAppViewModel: ViewModel() {
    protected val httpClient = HttpClient(getPlatformKtorEngine()) {
        install(ContentNegotiation) {
            json()
        }

        defaultRequest {
            url(API_BASE_URL)
        }
    }
}