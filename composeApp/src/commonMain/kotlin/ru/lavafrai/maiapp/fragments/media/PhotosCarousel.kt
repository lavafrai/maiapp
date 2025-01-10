package ru.lavafrai.maiapp.fragments.media

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PhotosCarousel(
    photos: List<String>,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier
        .height(300.dp)
        .background(color = Color.Red)
    ) {

    }
}