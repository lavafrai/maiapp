package ru.lavafrai.maiapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.*
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

    protected suspend fun emitAsync(newState: T) { _state.emit(newState) }

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

    protected fun launchCatching(onError: (Throwable) -> Unit, block: suspend CoroutineScope.() -> Unit) {
        val handler = CoroutineExceptionHandler { _, e ->
            e.printStackTrace()
            onError(e)
        }

        viewModelScope.launch {
            supervisorScope {
                launch(handler, block = block)
            }
        }
    }
}

suspend fun launchCatching(
    onError: (Throwable) -> Unit,
    block: suspend CoroutineScope.() -> Unit
) = coroutineScope {
    val handler = CoroutineExceptionHandler { _, e ->
        e.printStackTrace()
        onError(e)
    }

    launch {
        supervisorScope {
            launch(handler, block = block)
        }
    }
}