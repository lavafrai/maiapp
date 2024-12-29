package ru.lavafrai.maiapp.platform

import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import kotlinx.browser.window

class WebHapticFeedback: HapticFeedback {
    override fun performHapticFeedback(hapticFeedbackType: HapticFeedbackType) {
        val navigator = window.navigator
        when (hapticFeedbackType) {
            HapticFeedbackType.LongPress -> navigator.vibrate(50)
            HapticFeedbackType.TextHandleMove -> navigator.vibrate(10)
        }
    }
}