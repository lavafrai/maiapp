@file:OptIn(ExperimentalSharedTransitionApi::class)

package ru.lavafrai.maiapp.navigation.rootPages.teacherReviewsPage

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import compose.icons.FeatherIcons
import compose.icons.feathericons.ArrowLeft
import maiapp.composeapp.generated.resources.Res
import maiapp.composeapp.generated.resources.reviews
import org.jetbrains.compose.resources.stringResource
import ru.lavafrai.maiapp.data.BaseLoadableStatus
import ru.lavafrai.maiapp.fragments.ErrorView
import ru.lavafrai.maiapp.navigation.rootPages.main.MainPageTitle
import ru.lavafrai.maiapp.viewmodels.teacherReview.TeacherReviewViewModel
import ru.lavafrai.maiapp.viewmodels.teacherReview.TeacherReviewsView

@Composable
fun TeacherReviewsPage(
    teacherId: String,
    onNavigateBack: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
) {
    val viewModel: TeacherReviewViewModel = viewModel(factory = TeacherReviewViewModel.Factory(teacherId))
    val viewState by viewModel.state.collectAsState()

    Column {
        MainPageTitle(
            titleText = { Text(stringResource(Res.string.reviews)) },
            subtitleText = { Text(teacherId) },
            leftButton = { IconButton(onNavigateBack) { Icon(FeatherIcons.ArrowLeft, "Back icon") } },
        )

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f).fillMaxWidth(),
        ) {
            when (viewState.teacherInfo.baseStatus) {
                BaseLoadableStatus.Loading -> CircularProgressIndicator()

                BaseLoadableStatus.Error -> ErrorView(
                    error = viewState.teacherInfo.error,
                    onRetry = viewModel::startLoading,
                )

                BaseLoadableStatus.Actual -> TeacherReviewsView(
                    teacherInfo = viewState.teacherInfo.data!!,
                )
            }
        }
    }
}