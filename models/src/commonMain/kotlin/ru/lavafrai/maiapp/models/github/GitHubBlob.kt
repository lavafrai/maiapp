@file:OptIn(ExperimentalEncodingApi::class)

package ru.lavafrai.maiapp.models.github

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@Serializable
data class GitHubBlob(
    val sha: String,
    @SerialName("node_id") val nodeId: String,
    val size: Long,
    val url: String,
    val content: String,
    val encoding: String,
) {
    val decodedContent: String
        get() = when (encoding) {
            "base64" -> content.decodeBase64()
            else -> error("Unsupported encoding: $encoding")
        }

    private fun String.decodeBase64() = Base64.decode(this.replace("\n", "")).decodeToString()
}