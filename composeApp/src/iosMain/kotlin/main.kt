import androidx.compose.ui.window.ComposeUIViewController
import ru.lavafrai.maiapp.App
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController { App() }
