package ru.lavafrai.maiapp.platform

import kotlinx.coroutines.CoroutineDispatcher

class Dispatchers(
    val IO: CoroutineDispatcher,
    val Main: CoroutineDispatcher,
    val Default: CoroutineDispatcher,
)