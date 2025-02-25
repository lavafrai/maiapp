package ru.lavafrai.maiapp.models.maidata.asset

import io.ktor.client.*
import kotlinx.serialization.Serializable

@Serializable
sealed class Asset {
    abstract suspend fun load(loader: AssetLoader): ByteArray

    companion object {
        fun relative(url: String): Asset = RelativeAsset(url)
        fun url(url: String): Asset = UrlAsset(url)
        fun text(text: String): Asset = TextAsset(text)
        fun raw(data: ByteArray): Asset = RawAsset(data)
    }
}

class AssetLoader(
    val apiUrl: String,
    val httpClient: HttpClient,
) {
    companion object {
        fun forApi(apiUrl: String): AssetLoader {
            return AssetLoader(
                apiUrl = apiUrl,
                httpClient = HttpClient(),
            )
        }
    }
}