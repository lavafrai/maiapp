package ru.lavafrai.maiapp.viewmodels.teacherReview

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.lavafrai.maiapp.LocalApplicationContext
import ru.lavafrai.maiapp.fragments.PageColumn
import ru.lavafrai.maiapp.fragments.media.PhotosCarousel
import ru.lavafrai.maiapp.fragments.schedule.TeacherReviewView
import ru.lavafrai.maiapp.models.exler.ExlerTeacherInfo


data class CarouselItem(
    val id: Int,
    val imageUrl: String,
)

@Composable
fun ColumnScope.TeacherReviewsView(
    teacherInfo: ExlerTeacherInfo,
) {
    val appContext = LocalApplicationContext.current
    val photos = teacherInfo.photos
        ?.filter { !it.endsWith("Jeremy-Hillary-Boob-PhD_form-header.png") }
        ?.mapIndexed { index, it -> it } ?: emptyList()


    PageColumn (
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        paddings = false,
    ) {
        if (photos.isNotEmpty()) {
            PhotosCarousel(
                photos = photos,
                modifier = Modifier
                    .fillMaxWidth()
            )
        }

        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            teacherInfo.reviews.sortedBy { it.publishTime }.reversed().forEach { review ->
                TeacherReviewView(review)
            }

            Box(Modifier.windowInsetsPadding(WindowInsets.navigationBars))
        }
    }
}