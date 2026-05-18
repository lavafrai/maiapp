package ru.lavafrai.maiapp.fragments.shaker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
fun rememberShakeController(): ShakeController {
    return remember { ShakeController() }
}