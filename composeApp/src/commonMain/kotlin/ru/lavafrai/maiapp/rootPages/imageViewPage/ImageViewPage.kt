@file:OptIn(ExperimentalSharedTransitionApi::class, ExperimentalSharedTransitionApi::class)

package ru.lavafrai.maiapp.rootPages.imageViewPage

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import com.github.panpf.zoomimage.CoilZoomAsyncImage
import compose.icons.FeatherIcons
import compose.icons.feathericons.ArrowLeft
import compose.icons.feathericons.Copy
import compose.icons.feathericons.MoreVertical
import compose.icons.feathericons.Repeat
import compose.icons.feathericons.Share2
import maiapp.composeapp.generated.resources.Res
import maiapp.composeapp.generated.resources.copy
import maiapp.composeapp.generated.resources.share
import org.jetbrains.compose.resources.stringResource
import ru.lavafrai.maiapp.data.LoadableStatus
import ru.lavafrai.maiapp.platform.getPlatform
import ru.lavafrai.maiapp.utils.loadableImageUrl

@Composable
fun ImageViewPage(
    url: String,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onNavigateBack: () -> Unit,
) {
    var controlsVisible by remember { mutableStateOf(true) }
    var state by remember { mutableStateOf(LoadableStatus.Loading) }
    var retryHash by remember { mutableStateOf(0) }

    Surface(
        color = Color.Black,
        contentColor = Color.White,
        modifier = Modifier.fillMaxSize(),
    ) {
        Box(modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            with(sharedTransitionScope) {
                key(retryHash) {
                    CoilZoomAsyncImage(
                        model = remember(url) { loadableImageUrl(url) },
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .sharedElement(
                                rememberSharedContentState(url),
                                animatedVisibilityScope = animatedContentScope,
                            ),
                        contentScale = ContentScale.Fit,
                        onLoading = { state = LoadableStatus.Loading },
                        onError = { state = LoadableStatus.Error },
                        onSuccess = { state = LoadableStatus.Actual },
                        scrollBar = null,
                        onTap = { controlsVisible = !controlsVisible },
                    )
                }
            }

            if (state == LoadableStatus.Loading) CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            if (state == LoadableStatus.Error) IconButton(onClick = { retryHash++ }) {
                Icon(
                    imageVector = FeatherIcons.Repeat,
                    contentDescription = "repeat",
                    modifier = Modifier.align(Alignment.Center),
                )
            }
            ImageViewControls(
                visible = controlsVisible,
                onNavigateBack = onNavigateBack,
                shareUrl = url,
            )
        }
    }
}

@Composable
fun ImageViewControls(
    visible: Boolean,
    onNavigateBack: () -> Unit,
    shareUrl: String,
) {
    val clipboardManager = LocalClipboardManager.current
    val platform = getPlatform()
    val copy = { data: String -> clipboardManager.setText(buildAnnotatedString {
        append(data)
    }) }
    val share = platform::shareText

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(50, easing = EaseInOut)),
        exit = fadeOut(animationSpec = tween(50, easing = EaseInOut)),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = FeatherIcons.ArrowLeft,
                        contentDescription = "back",
                    )
                }
                Spacer(modifier = Modifier.weight(1f))

                Box {
                    var menuExpanded by remember { mutableStateOf(false) }
                    IconButton(onClick = { menuExpanded = !menuExpanded }) {
                        Icon(
                            imageVector = FeatherIcons.MoreVertical,
                            contentDescription = "repeat",
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            leadingIcon = { Icon(imageVector = FeatherIcons.Copy, contentDescription = "copy")},
                            text = { Text(stringResource(Res.string.copy)) },
                            onClick = { menuExpanded = false ; copy(shareUrl) }
                        )
                        DropdownMenuItem(
                            leadingIcon = { Icon(imageVector = FeatherIcons.Share2, contentDescription = "share")},
                            text = { Text(stringResource(Res.string.share)) },
                            onClick = { menuExpanded = false ; share(shareUrl) },
                            enabled = platform.supportsShare()
                        )
                    }
                }
            }
        }
    }
}