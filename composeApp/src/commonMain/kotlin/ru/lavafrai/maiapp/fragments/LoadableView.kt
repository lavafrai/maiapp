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
fun <T>LoadableView(
    state: Loadable<T>,
    error: @Composable (Throwable?, () -> Unit) -> Unit = { e, onRetry -> ErrorView(e, onRetry) },
    loading: @Composable () -> Unit = { DefaultLoadingView() },
    retry: () -> Unit,
    alignment: Alignment = Alignment.Center,
    modifier: Modifier = Modifier.fillMaxSize(),
    content: @Composable (T) -> Unit,
) {
    MaterialSharedAxisY(targetState = state.baseStatus, forward = true) { status ->
        Box(modifier = modifier, contentAlignment = alignment) {
            when (status) {
                BaseLoadableStatus.Loading -> loading()
                BaseLoadableStatus.Error -> error(state.error, retry)
                BaseLoadableStatus.Actual -> content(state.data!!)
                // else -> error(IllegalStateException("Unknown base status: $state")) { retry() }
            }
        }
    }
}

@Composable
fun DefaultLoadingView() {
    CircularProgressIndicator()
}