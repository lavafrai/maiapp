package ru.lavafrai.maiapp.fragments.media

import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorProducer
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.svg.SvgDecoder
import io.ktor.utils.io.core.*

@Composable
fun SvgIcon(
    svg: ByteArray,
    contentDescription: String? = null,
    modifier: Modifier = Modifier.size(24.dp),
    tint: Color? = null,
) {
    val context = LocalPlatformContext.current
    val imageLoader = ImageLoader.Builder(context)
        .components {
            add(SvgDecoder.Factory())
        }
        .build()

    AsyncImage(
        model = remember(svg) { svg },
        contentDescription = contentDescription,
        imageLoader = imageLoader,
        onError = { error ->
            error("Error loading image: $error")
        },
        modifier = modifier,
        colorFilter = ColorFilter.tint(tint ?: LocalContentColor.current),
    )
}