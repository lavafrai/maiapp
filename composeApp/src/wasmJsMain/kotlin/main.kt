import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.window.ComposeViewport
import ru.lavafrai.maiapp.App
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.Navigator
import ru.lavafrai.maiapp.platform.WebHapticFeedback

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val body = document.body ?: return
    val webHapticFeedback = WebHapticFeedback()
    ComposeViewport(body) {
        CompositionLocalProvider(LocalHapticFeedback provides webHapticFeedback) {
            App()
        }
    }
}
