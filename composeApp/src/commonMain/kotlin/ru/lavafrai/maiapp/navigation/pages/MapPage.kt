package ru.lavafrai.maiapp.navigation.pages

import kotlinx.serialization.Serializable

@Serializable
data class MapPage(
    val url: String,
    val title: String,
)