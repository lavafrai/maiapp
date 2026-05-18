package ru.lavafrai.maiapp.viewmodels.webview

import ru.lavafrai.maiapp.data.Loadable

data class WebviewViewState(
    val page: Loadable<String>,
)