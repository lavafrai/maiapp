import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import ru.lavafrai.maiapp.App
import kotlinx.browser.document

external fun onWasmLoaded()

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val body = document.body ?: return
    ComposeViewport(body) {
        App()
    }

    hidePreloader()
}

fun hidePreloader() {
    onWasmLoaded()
}
