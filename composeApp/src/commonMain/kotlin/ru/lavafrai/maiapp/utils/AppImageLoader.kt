package ru.lavafrai.maiapp.utils

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.annotation.ExperimentalCoilApi
import coil3.network.DeDupeConcurrentRequestStrategy
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.svg.SvgDecoder
import ru.lavafrai.maiapp.platform.trimMemoryCacheInBackground

@OptIn(ExperimentalCoilApi::class)
fun buildAppImageLoader(context: PlatformContext): ImageLoader = ImageLoader.Builder(context)
    .components {
        add(KtorNetworkFetcherFactory(concurrentRequestStrategy = { DeDupeConcurrentRequestStrategy() }))
        add(SvgDecoder.Factory())
    }
    .trimMemoryCacheInBackground()
    .build()
