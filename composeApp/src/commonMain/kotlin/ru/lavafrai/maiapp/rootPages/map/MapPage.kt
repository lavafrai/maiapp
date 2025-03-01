@file:OptIn(ExperimentalResourceApi::class)

package ru.lavafrai.maiapp.rootPages.map

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.panpf.zoomimage.ZoomImage
import com.github.panpf.zoomimage.compose.rememberZoomState
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.fromByteArray
import com.github.panpf.zoomimage.util.Logger
import compose.icons.FeatherIcons
import compose.icons.feathericons.ArrowLeft
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToImageBitmap
import ru.lavafrai.maiapp.fragments.LoadableView
import ru.lavafrai.maiapp.rootPages.main.MainPageTitle
import ru.lavafrai.maiapp.viewmodels.webview.MapViewModel


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MapPage(
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onNavigateBack: () -> Unit,
    url: String,
    title: String,
) = Column(modifier = Modifier.fillMaxSize()) {
    val viewModel: MapViewModel = viewModel(factory = MapViewModel.Factory(url, title))
    val viewState by viewModel.state.collectAsState()

    MainPageTitle(
        titleText = { Text(title) },
        leftButton = {
            IconButton(onClick = onNavigateBack) {
                Icon(FeatherIcons.ArrowLeft, contentDescription = "back")
            }
        }
    )

    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
    ) {
        LoadableView(
            viewState.data,
            retry = viewModel::refresh,
            modifier = Modifier.fillMaxSize(),
        ) { data ->
            val zoomState = rememberZoomState(logLevel = Logger.Level.Verbose)
            val image = remember(data) { data.decodeToImageBitmap() }
            // val painter = remember(data) { BitmapPainter(image) }
            val thumbnail = remember(data) {
                val thumbnailSize = Size(image.width.div(10).toFloat(), image.height.div(10).toFloat())
                val thumbnailPainter = object : Painter() {
                    override val intrinsicSize = thumbnailSize
                    override fun DrawScope.onDraw() {
                        //drawImage(image, size = thumbnailSize)
                    }
                }
                thumbnailPainter
            }

            LaunchedEffect(zoomState.subsampling) {
                //val resUri = Res.getUri("drawable/map.png")
                val imageSource = ImageSource.fromByteArray(data)
                zoomState.setSubsamplingImage(imageSource)
            }

            ZoomImage(
                painter = thumbnail,
                contentDescription = "map",
                modifier = Modifier.fillMaxSize(),
                zoomState = zoomState,
            )
        }
    }
}