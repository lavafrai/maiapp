@file:OptIn(ExperimentalWasmJsInterop::class)

package ru.lavafrai.maiapp.platform

import androidx.navigation.NavController
import androidx.navigation.NavGraph
import kotlinx.browser.window
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.w3c.dom.PopStateEvent
import org.w3c.dom.events.Event

/**
 * Makes the back button and gesture of the browser go back in the app instead of leaving the page.
 *
 * Every app screen gets a browser history entry with the back stack depth as its state. The navigation library
 * has bindToBrowserNavigation for this, but it keeps the routes with their arguments in the history, and some
 * screens get a whole schedule as an argument. So the browser forward button can't restore a screen and is undone.
 */
suspend fun NavController.bindToBrowserBack(): Unit = coroutineScope {
    fun depth() = currentBackStack.value.count { it.destination !is NavGraph }

    // The NavHost fills the back stack in the first composition
    currentBackStack.first { depth() > 0 }
    var historyDepth = depth()
    window.history.replaceState(historyDepth.toJsNumber(), "")

    launch {
        popStates().collect { state ->
            val target = (state as? JsNumber)?.toInt() ?: return@collect
            val appDepth = depth()
            if (target in 1..<appDepth) {
                historyDepth = target
                repeat(appDepth - target) { popBackStack() }
            } else if (target > appDepth) {
                window.history.go(appDepth - target)
            }
        }
    }
    currentBackStack.collect {
        val appDepth = depth()
        if (appDepth > historyDepth) window.history.pushState(appDepth.toJsNumber(), "")
        // The app went back itself, e.g. with its back button
        else if (appDepth < historyDepth) window.history.go(appDepth - historyDepth)
        historyDepth = appDepth
    }
}

private fun popStates(): Flow<JsAny?> = callbackFlow {
    val listener: (Event) -> Unit = { trySend((it as PopStateEvent).state) }
    window.addEventListener("popstate", listener)
    awaitClose { window.removeEventListener("popstate", listener) }
}
