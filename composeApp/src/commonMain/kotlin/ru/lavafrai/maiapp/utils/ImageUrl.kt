package ru.lavafrai.maiapp.utils

import net.thauvin.erik.urlencoder.UrlEncoderUtil
import ru.lavafrai.maiapp.BuildConfig
import ru.lavafrai.maiapp.platform.getPlatform

/**
 * URL to load an image of another site from. Browsers can't draw such images without CORS headers,
 * which the exler sites with teacher photos don't send, so there they go through our server
 */
fun loadableImageUrl(url: String): String {
    if (getPlatform().canLoadCrossOriginImages() || url.startsWith(BuildConfig.API_BASE_URL)) return url
    // Without the locale unlike other requests to our server: photos don't depend on it, and it would split their cache
    return "${BuildConfig.API_BASE_URL}/exler-photo?url=${UrlEncoderUtil.encode(url)}"
}
