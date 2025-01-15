@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)

package ru.lavafrai.maiapp.fragments.media

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy
import coil3.request.crossfade
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import maiapp.composeapp.generated.resources.Res
import maiapp.composeapp.generated.resources.ic_cyclone
import maiapp.composeapp.generated.resources.mai_logo
import maiapp.composeapp.generated.resources.refresh
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.compose.resources.painterResource
import ru.lavafrai.maiapp.data.LoadableStatus
import ru.lavafrai.maiapp.fragments.schedule.TeacherPhoto
import ru.lavafrai.maiapp.utils.conditional

@Composable
fun PhotosCarousel(
    photos: List<String>,
    fullSizeUrls: Map<String, String>,
    modifier: Modifier = Modifier,
    innerImagesPadding: Dp = 8.dp,
    horizontalImagesPadding: Dp = 0.dp,
    height: Dp = 300.dp,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
) {
    val shimmerInstance = rememberShimmer(shimmerBounds = ShimmerBounds.Window)

    Box(modifier = modifier
        .height(height)
        //.background(color = Color.Red)
    ) {
        Row(
            modifier = Modifier
                .matchParentSize(),
            horizontalArrangement = Arrangement.Center,
        ) {
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .fillMaxHeight(),
                horizontalArrangement = Arrangement.spacedBy(innerImagesPadding),
            ) {
                Spacer(Modifier.width(horizontalImagesPadding))

                photos.forEach { photoUrl ->
                    with (sharedTransitionScope) {
                        val fullSizeUrl = fullSizeUrls[photoUrl] ?: photoUrl
                        TeacherPhoto(
                            url = photoUrl,
                            fullSizeUrl = fullSizeUrl,
                            shimmerInstance = shimmerInstance,
                            modifier = Modifier
                                .sharedElement(
                                    rememberSharedContentState(key = fullSizeUrl),
                                    animatedVisibilityScope = animatedContentScope,
                                ),
                        )
                    }
                }

                Spacer(Modifier.width(horizontalImagesPadding))
            }
        }
    }
}