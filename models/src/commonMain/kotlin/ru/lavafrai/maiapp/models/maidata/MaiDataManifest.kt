package ru.lavafrai.maiapp.models.maidata

import kotlinx.serialization.Serializable

@Serializable
data class MaiDataManifest(
    val version: Int,
    val data: List<MaiDataItem>,
)