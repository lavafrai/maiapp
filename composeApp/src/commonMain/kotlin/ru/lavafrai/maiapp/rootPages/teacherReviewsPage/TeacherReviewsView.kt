@file:OptIn(ExperimentalSharedTransitionApi::class)

package ru.lavafrai.maiapp.rootPages.teacherReviewsPage

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import compose.icons.FeatherIcons
import compose.icons.feathericons.Calendar
import compose.icons.feathericons.ExternalLink
import compose.icons.feathericons.Link
import maiapp.composeapp.generated.resources.Res
import maiapp.composeapp.generated.resources.open_on_exler
import maiapp.composeapp.generated.resources.schedule
import org.jetbrains.compose.resources.stringResource
import ru.lavafrai.maiapp.LocalApplicationContext
import ru.lavafrai.maiapp.fragments.PageColumn
import ru.lavafrai.maiapp.fragments.media.PhotosCarousel
import ru.lavafrai.maiapp.fragments.schedule.TeacherReviewView
import ru.lavafrai.maiapp.models.exler.ExlerTeacherInfo
import ru.lavafrai.maiapp.models.schedule.TeacherUid
import ru.lavafrai.maiapp.utils.asDp


data class CarouselItem(
    val id: Int,
    val imageUrl: String,
)

@Composable
fun ColumnScope.TeacherReviewsView(
    teacherInfo: ExlerTeacherInfo,
    teacherUid: TeacherUid,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
) {
    val appContext = LocalApplicationContext.current
    val photos = teacherInfo.photos
    val fullSizeUrls = teacherInfo.largePhotos

    PageColumn (
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        paddings = false,
    ) {
        Spacer(Modifier.height(8.dp))

        if (photos.isNotEmpty()) {
            PhotosCarousel(
                photos = photos,
                fullSizeUrls = fullSizeUrls,
                modifier = Modifier
                    .fillMaxWidth(),
                sharedTransitionScope = sharedTransitionScope,
                animatedContentScope = animatedContentScope,
            )
        }

        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (teacherUid != TeacherUid.Empty) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        appContext.openSchedule(teacherUid, title = teacherInfo.name)
                    }
                ) {
                    Icon(
                        imageVector = FeatherIcons.Calendar,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(Res.string.schedule))
                }
            }

            teacherInfo.reviews.sortedBy { it.publishTime }.reversed().forEach { review ->
                TeacherReviewView(review)
            }

            InputChip(
                onClick = {
                    appContext.openUrl(teacherInfo.link, forcedInBrowser = true)
                },
                modifier = Modifier
                    .align(Alignment.End),
                label = {
                    Text(stringResource(Res.string.open_on_exler))
                },
                selected = false,
                trailingIcon = {
                    Icon(
                        imageVector = FeatherIcons.ExternalLink,
                        contentDescription = null,
                        modifier = Modifier.size(LocalTextStyle.current.fontSize.asDp),
                        tint = LocalContentColor.current,
                    )
                },
            )

            Box(Modifier.windowInsetsPadding(WindowInsets.navigationBars))
        }
    }
}