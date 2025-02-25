package ru.lavafrai.maiapp.models.maidata.asset

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("text")
class TextAsset(
    val text: String,
): Asset() {
    override suspend fun load(loader: AssetLoader): ByteArray = text.encodeToByteArray()
}