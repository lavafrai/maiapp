@file:OptIn(ExperimentalMaterial3Api::class)

package ru.lavafrai.maiapp.viewmodels.teacherReview

import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.lavafrai.maiapp.LocalApplicationContext
import ru.lavafrai.maiapp.fragments.PageColumn
import ru.lavafrai.maiapp.fragments.schedule.TeacherReviewView
import ru.lavafrai.maiapp.models.exler.ExlerTeacherInfo
import ru.lavafrai.maiapp.platform.getPlatform


data class CarouselItem(
    val id: Int,
    val imageUrl: String,
)

@Composable
fun ColumnScope.TeacherReviewsView(
    teacherInfo: ExlerTeacherInfo,
) {
    val openUrl = { url: String ->
        val platform = getPlatform()
        val defaultUrl = "https://mai-exler.ru"
        val fullUrl = if (url.startsWith("http")) url else "$defaultUrl$url"
        platform.openUrl(fullUrl)
    }
    val appContext = LocalApplicationContext.current
    val photos = teacherInfo.photo
        ?.filter { !it.endsWith("Jeremy-Hillary-Boob-PhD_form-header.png") }
        ?.mapIndexed { index, it -> CarouselItem(index, it) } ?: emptyList()

    PageColumn (
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        teacherInfo.reviews.sortedBy { it.publishTime }.reversed().forEach { review ->
            TeacherReviewView(review)
        }

        Box(Modifier.windowInsetsPadding(WindowInsets.navigationBars))
    }
}