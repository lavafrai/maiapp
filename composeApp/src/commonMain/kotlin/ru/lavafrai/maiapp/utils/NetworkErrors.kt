package ru.lavafrai.maiapp.utils

import io.ktor.util.network.UnresolvedAddressException
import ru.lavafrai.maiapp.platform.getPlatform

/** The device is offline or can't resolve the server. Each HTTP engine reports it with its own exception */
fun Throwable.isNoConnectionError(): Boolean = generateSequence(this) { it.cause }.any {
    it is UnresolvedAddressException || getPlatform().isNoConnectionError(it) // CIO or the platform engine
}
