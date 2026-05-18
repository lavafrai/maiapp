package ru.lavafrai.maiapp.models.exceptions

class AssetLoadingException(message: String? = null, cause: Throwable? = null): MaiAppException(message, cause) {
    override fun getReadableDescription(): String {
        return "Failed to load asset: ${message ?: cause?.message ?: "Unknown error"}"
    }
}