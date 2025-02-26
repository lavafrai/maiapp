package ru.lavafrai.maiapp.navigation.pages

import kotlinx.serialization.Serializable

@Serializable
data class WebviewPage(
    val url: String,
    val title: String,
)