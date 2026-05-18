@file:OptIn(ExperimentalSharedTransitionApi::class)

package ru.lavafrai.maiapp.rootPages.teacherReviewsPage

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import compose.icons.FeatherIcons
import compose.icons.feathericons.ArrowLeft
import maiapp.composeapp.generated.resources.Res
import maiapp.composeapp.generated.resources.reviews
import org.jetbrains.compose.resources.stringResource
import ru.lavafrai.maiapp.fragments.LoadableView
import ru.lavafrai.maiapp.models.schedule.TeacherUid
import ru.lavafrai.maiapp.rootPages.main.MainPageTitle
import ru.lavafrai.maiapp.viewmodels.teacherReview.TeacherReviewViewModel

@Composable
fun TeacherReviewsPage(
    teacherId: String,
    teacherUid: TeacherUid,
    onNavigateBack: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
) {
    val viewModel: TeacherReviewViewModel = viewModel(factory = TeacherReviewViewModel.Factory(teacherId, teacherUid))
    val viewState by viewModel.state.collectAsState()

    Column {
        MainPageTitle(
            titleText = { Text(stringResource(Res.string.reviews)) },
            subtitleText = { Text(teacherId) },
            leftButton = { IconButton(onNavigateBack) { Icon(FeatherIcons.ArrowLeft, "Back icon") } },
        )

        LoadableView(
            state = viewState.teacherInfo,
            retry = viewModel::startLoading,
            modifier = Modifier.fillMaxSize(),
        ) {
            TeacherReviewsView(
                teacherInfo = it,
                teacherUid = teacherUid,
                sharedTransitionScope,
                animatedContentScope,
            )
        }
    }
}