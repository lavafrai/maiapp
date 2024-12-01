@file:OptIn(ExperimentalMaterial3Api::class)

package ru.lavafrai.maiapp.viewmodels.teacherReview

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalUncontainedCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import ru.lavafrai.maiapp.models.exler.ExlerTeacherInfo


data class CarouselItem(
    val id: Int,
    val imageUrl: String,
)

@Composable
fun TeacherReviewsView(
    teacherInfo: ExlerTeacherInfo,
) {
    val items = teacherInfo.photo
        ?.filter { !it.endsWith("Jeremy-Hillary-Boob-PhD_form-header.png") }
        ?.mapIndexed { index, it -> CarouselItem(index, it) } ?: emptyList()

    Column {
        /*HorizontalUncontainedCarousel(
            state = rememberCarouselState { items.count() },
            itemWidth = 200.dp,
            itemSpacing = 8.dp,
            contentPadding = PaddingValues(horizontal = 16.dp),
            modifier = Modifier.fillMaxWidth(),
        ) { index ->
            val item = items[index]
            AsyncImage(
                model = item.imageUrl,
                contentDescription = null,
                modifier = Modifier.height(205.dp).maskClip(MaterialTheme.shapes.extraLarge),
            )
        }*/

        Text("Loaded!")
    }
}