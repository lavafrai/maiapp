package ru.lavafrai.maiapp.viewmodels

import androidx.lifecycle.ViewModel
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.Json
import ru.lavafrai.maiapp.platform.getPlatform

open class MaiAppViewModel<T>(
    protected val initialState: T
): ViewModel() {
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<T> = _state


    val dispatchers = getPlatform().dispatchers()

    protected fun emit(newState: T) { _state.value = newState }
    protected val stateValue
        get() = _state.value

    protected val httpClient = HttpClient(getPlatform().ktorEngine()) {
        followRedirects = true

        install(ContentNegotiation) {
            json(
                json = Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                }
            )
        }

        defaultRequest {
            // url(API_BASE_URL)
            headers {
                append("Accept", "application/json")
            }
        }
    }
}