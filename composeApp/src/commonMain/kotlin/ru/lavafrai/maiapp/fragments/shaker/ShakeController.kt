package ru.lavafrai.maiapp.fragments.shaker

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class ShakeController {
    var config: ShakeConfig? by mutableStateOf(null)
        private set

    fun shake(config: ShakeConfig) {
        this.config = config
    }
}