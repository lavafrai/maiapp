package ru.lavafrai.maiapp.viewmodels.webview

import ru.lavafrai.maiapp.data.Loadable

data class MapViewState(
    val data: Loadable<ByteArray>,
)