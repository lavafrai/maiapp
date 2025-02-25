@file:OptIn(ExperimentalFoundationApi::class)

package ru.lavafrai.maiapp.fragments.media

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.svg.SvgDecoder
import compose.icons.FeatherIcons
import compose.icons.feathericons.AlertOctagon
import io.ktor.utils.io.core.*
import ru.lavafrai.maiapp.data.Loadable
import ru.lavafrai.maiapp.fragments.LoadableView

@Composable
fun SvgIcon(
    svg: String,
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
        model = remember(svg) { svg.toByteArray() },
        contentDescription = contentDescription,
        imageLoader = imageLoader,
        onError = { error ->
            error("Error loading image: $error")
        },
        modifier = modifier,
        colorFilter = ColorFilter.tint(tint ?: LocalContentColor.current),
    )
}

@Composable
fun LoadableSvgIcon(
    svg: Loadable<String>,
    contentDescription: String? = null,
    modifier: Modifier = Modifier.size(24.dp),
    tint: Color? = null,
    reload: () -> Unit = {},
) {
    LoadableView(
        svg,
        retry = {},
        loading = { CircularProgressIndicator(modifier = Modifier.size(24.dp)) },
        error = { e, r -> LoadableSvgIconReload(reload, e) },
        alignment = Alignment.TopStart,
        modifier = Modifier.size(24.dp),
        animated = false,
    ) { icon ->
        SvgIcon(
            icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun LoadableSvgIconReload(
    reload: () -> Unit,
    error: Throwable?,
) {
    val clipboard = LocalClipboardManager.current
    val hapticFeedback = LocalHapticFeedback.current

    Box(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .size(24.dp)
            .combinedClickable(
                onClick = reload,
                onLongClick = {
                    clipboard.setText(buildAnnotatedString {
                        append(error?.stackTraceToString())
                    })
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            ),
    ) {
        Icon(
            FeatherIcons.AlertOctagon,
            contentDescription = "Error loading icon",
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.error,
        )
    }
}