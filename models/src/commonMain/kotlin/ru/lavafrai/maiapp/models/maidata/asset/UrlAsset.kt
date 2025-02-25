package ru.lavafrai.maiapp.models.maidata.asset

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.lavafrai.maiapp.models.exceptions.AssetLoadingException

@Serializable
@SerialName("url")
class UrlAsset(
    val url: String,
): Asset() {
    override suspend fun load(loader: AssetLoader): ByteArray {
        val httpClient = loader.httpClient

        val response = try {
            httpClient.get(url)
        } catch (e: Exception) {
            throw AssetLoadingException("Failed to load asset, no connection", e)
        }

        if (!response.status.isSuccess()) {
            throw AssetLoadingException("Failed to load asset from url: ${response.status}")
        }

        return response.bodyAsBytes()
    }
}