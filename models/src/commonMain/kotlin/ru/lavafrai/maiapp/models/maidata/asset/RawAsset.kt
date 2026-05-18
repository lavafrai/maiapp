package ru.lavafrai.maiapp.models.maidata.asset

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("raw")
class RawAsset(
    val data: ByteArray,
): Asset() {
    override suspend fun load(loader: AssetLoader): ByteArray = data
}