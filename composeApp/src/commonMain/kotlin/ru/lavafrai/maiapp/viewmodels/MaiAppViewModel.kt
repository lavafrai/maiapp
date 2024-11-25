package ru.lavafrai.maiapp.viewmodels

import androidx.lifecycle.ViewModel
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.lavafrai.maiapp.BuildConfig.API_BASE_URL
import ru.lavafrai.maiapp.platform.getPlatformDispatchers
import ru.lavafrai.maiapp.platform.getPlatformKtorEngine

open class MaiAppViewModel<T>(
    protected val initialState: T
): ViewModel() {
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<T> = _state

    val dispatchers = getPlatformDispatchers()

    protected suspend fun emit(newState: T) = _state.emit(newState)
    protected val stateValue
        get() = _state.value

    protected val httpClient = HttpClient(getPlatformKtorEngine()) {
        followRedirects = true

        install(ContentNegotiation) {
            json()
        }

        defaultRequest {
            // url(API_BASE_URL)
            headers {
                append("Accept", "application/json")
            }
        }
    }
}