@file:OptIn(ExperimentalMaterial3WindowSizeClassApi::class)

package ru.lavafrai.maiapp.utils

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.pageColumnPaddings(
    horizontal: Boolean = false
): Modifier {
    val widthSizeClass = calculateWindowSizeClass().widthSizeClass
    return when (widthSizeClass) {
        WindowWidthSizeClass.Expanded -> this
            .widthIn(max = 800.dp)
            .fillMaxWidth(0.8f)
        else -> this.conditional(horizontal) { padding(horizontal = 8.dp) }
    }
}