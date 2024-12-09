@file:OptIn(ExperimentalMaterial3Api::class)

package ru.lavafrai.maiapp.viewmodels.teacherReview

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.lavafrai.maiapp.LocalApplicationContext
import ru.lavafrai.maiapp.fragments.AppCard
import ru.lavafrai.maiapp.fragments.hypertext.Hypertext
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
    val reviewHypertexts = teacherInfo.reviews
        .map { it.hypertext ?: "null" }
        .map { it.replace("\\n", "\n") }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .verticalScroll(rememberScrollState()),
    ) {
        reviewHypertexts.forEach { reviewHypertext ->
            AppCard {
                Hypertext(reviewHypertext)
            }
        }

        Box(Modifier.windowInsetsPadding(WindowInsets.navigationBars))
    }
}