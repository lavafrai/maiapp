package ru.lavafrai.maiapp.fragments.media

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
fun PhotosCarousel(
    photos: List<String>,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier
        .height(300.dp)
        .background(color = Color.Red)
    ) {
        Row(
            modifier = Modifier
                .matchParentSize()
                .horizontalScroll(rememberScrollState())
        ) {
            photos.forEach { photoUrl ->
                AsyncImage(
                    model = photoUrl,
                    contentDescription = "photo of teacher",
                    modifier = Modifier.fillMaxHeight(),
                    contentScale = ContentScale.FillHeight,
                )
            }
        }
    }
}