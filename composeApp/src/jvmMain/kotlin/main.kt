import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import maiapp.composeapp.generated.resources.Res
import maiapp.composeapp.generated.resources.icon
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import ru.lavafrai.maiapp.App
import java.awt.Dimension

fun main() = application {
    Window(
        title = "MAI app",
        icon = painterResource(Res.drawable.icon),
        state = rememberWindowState(width = 800.dp, height = 600.dp),
        onCloseRequest = ::exitApplication,
    ) {
        window.minimumSize = Dimension(350, 600)
        App()
    }
}

@Preview
@Composable
fun AppPreview() { App() }