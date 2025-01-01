package ru.lavafrai.maiapp.utils

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope


suspend fun <T, R> List<T>.mapAsync(
    block: suspend (T) -> R,
): List<R> = coroutineScope {
    val tasks = map { element -> async { block(element) }}
    tasks.map { it.await() }
}