package ru.lavafrai.maiapp.viewmodels.webview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.launch
import ru.lavafrai.maiapp.data.Loadable
import ru.lavafrai.maiapp.models.exceptions.AssetLoadingException
import ru.lavafrai.maiapp.viewmodels.MaiAppViewModel
import kotlin.reflect.KClass

class MapViewModel(
    val url: String,
    val title: String,
): MaiAppViewModel<MapViewState>(
    initialState = MapViewState(
        data = Loadable.loading()
    )
) {
    init {
        refresh()
    }

    fun refresh() {
        emit(initialState)

        viewModelScope.launch {
            launchCatching(onError = { emit(stateValue.copy(data = Loadable.error(it))) }) {
                val page = httpClient
                    .get(url)

                if (!page.status.isSuccess()) throw AssetLoadingException("HTTP ${page.status}")
                val text = page.bodyAsBytes()
                if (text.isEmpty()) throw AssetLoadingException("Empty page")

                emit(stateValue.copy(data = Loadable.actual(text)))
            }
        }
    }

    class Factory(
        val url: String,
        val title: String,
    ): ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
            return MapViewModel(
                url = url,
                title = title,
            ) as T
        }
    }
}