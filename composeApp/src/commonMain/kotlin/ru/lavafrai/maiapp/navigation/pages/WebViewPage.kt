package ru.lavafrai.maiapp.navigation.pages

import kotlinx.serialization.Serializable

@Serializable
data class WebViewPage(
    val url: String,
    val title: String,
)