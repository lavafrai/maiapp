@file:OptIn(ExperimentalSharedTransitionApi::class)

package ru.lavafrai.maiapp.fragments.schedule

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy
import coil3.request.crossfade
import com.valentinilk.shimmer.Shimmer
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import compose.icons.FeatherIcons
import compose.icons.feathericons.Repeat
import ru.lavafrai.maiapp.LocalApplicationContext
import ru.lavafrai.maiapp.data.LoadableStatus
import ru.lavafrai.maiapp.utils.conditional

@Composable
fun TeacherPhoto(
    url: String,
    fullSizeUrl: String,
    shimmerInstance: Shimmer = rememberShimmer(shimmerBounds = ShimmerBounds.View),
    modifier: Modifier = Modifier,
) {
        val appContext = LocalApplicationContext.current
        val context = LocalPlatformContext.current
        var retryHash by remember { mutableStateOf(0) }
        val imageLoader = remember(retryHash) {
            ImageLoader.Builder(context)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .crossfade(200)
                .crossfade(true)
                .build()
        }

        var state by remember { mutableStateOf(LoadableStatus.Loading) }

        Box(modifier = Modifier.fillMaxHeight()) {
            AsyncImage(
                model = url,
                imageLoader = imageLoader,
                contentDescription = "photo of teacher",
                modifier = modifier
                    .fillMaxHeight()
                    .clip(MaterialTheme.shapes.medium)
                    .conditional(state == LoadableStatus.Loading) { shimmer(customShimmer = shimmerInstance) }
                    .conditional(state != LoadableStatus.Actual) { widthIn(min = 200.dp) }
                    .conditional(state == LoadableStatus.Actual) { clickable { appContext.openImageView(fullSizeUrl) } }
                    .conditional(state == LoadableStatus.Error) { clickable { retryHash++ } }
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.FillHeight,
                onError = { state = LoadableStatus.Error },
                onLoading = { state = LoadableStatus.Loading },
                onSuccess = { state = LoadableStatus.Actual },
            )

            if (state == LoadableStatus.Error) Icon(
                FeatherIcons.Repeat,
                "Retry",
                modifier = Modifier
                    .align(Alignment.Center)
                    .clickable { retryHash++ }
            )

            if (state == LoadableStatus.Loading) CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
            )
        }
}