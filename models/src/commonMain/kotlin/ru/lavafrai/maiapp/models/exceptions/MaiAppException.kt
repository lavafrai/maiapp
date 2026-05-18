package ru.lavafrai.maiapp.models.exceptions

abstract class MaiAppException(message: String? = null, cause: Throwable? = null): Exception(message, cause) {
    abstract fun getReadableDescription(): String
}