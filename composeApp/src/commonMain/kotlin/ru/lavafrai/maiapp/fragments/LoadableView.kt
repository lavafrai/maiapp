package ru.lavafrai.maiapp.fragments

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ru.lavafrai.maiapp.data.BaseLoadableStatus
import ru.lavafrai.maiapp.data.Loadable
import soup.compose.material.motion.MaterialSharedAxisY

@Composable
fun <T> LoadableView(
    state: Loadable<T>,
    error: @Composable (Throwable?, () -> Unit) -> Unit = { e, onRetry -> ErrorView(e, onRetry) },
    loading: @Composable () -> Unit = { DefaultLoadingView() },
    retry: () -> Unit,
    alignment: Alignment = Alignment.Center,
    modifier: Modifier = Modifier.fillMaxSize(),
    animated: Boolean = true,
    content: @Composable (T) -> Unit,
) {
    if (animated) MaterialSharedAxisY(
        targetState = state.baseStatus,
        forward = true,
        modifier = modifier,
    ) { status ->
        Box(modifier = modifier, contentAlignment = alignment) {
            when (status) {
                BaseLoadableStatus.Loading -> loading()
                BaseLoadableStatus.Error -> error(state.error, retry)
                BaseLoadableStatus.Actual -> if (state.data != null) content(state.data)
                // else -> error(IllegalStateException("Unknown base status: $state")) { retry() }
            }
        }
    }
    else Box(modifier = modifier, contentAlignment = alignment) {
        when (state.baseStatus) {
            BaseLoadableStatus.Loading -> loading()
            BaseLoadableStatus.Error -> error(state.error, retry)
            BaseLoadableStatus.Actual -> if (state.data != null) content(state.data)
            // else -> error(IllegalStateException("Unknown base status: $state")) { retry() }
        }
    }
}

@Composable
fun DefaultLoadingView() {
    CircularProgressIndicator()
}