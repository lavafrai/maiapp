package ru.lavafrai.maiapp.platform

import io.ktor.client.engine.*


expect fun getPlatformName(): String
expect fun getPlatformKtorEngine(): HttpClientEngineFactory<*>
