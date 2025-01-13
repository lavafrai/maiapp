@file:OptIn(ExperimentalMaterial3Api::class)

package ru.lavafrai.maiapp.fragments.media

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
fun PhotosCarousel(
    photos: List<String>,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(pageCount = { photos.size })

    Box(modifier = modifier
        .height(300.dp)
        .background(color = Color.Red)
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
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                photos.forEach { photoUrl ->
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = "photo of teacher",
                        modifier = Modifier
                            .fillMaxHeight()
                            .clip(MaterialTheme.shapes.medium),
                        contentScale = ContentScale.FillHeight,
                        onError = { /* TODO */ },
                        onLoading = { /* TODO */ },
                        onSuccess = { /* TODO */ },
                    )
                }
            }
        }
    }
}