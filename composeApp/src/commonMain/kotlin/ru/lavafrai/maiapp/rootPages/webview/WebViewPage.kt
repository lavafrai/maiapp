package ru.lavafrai.maiapp.rootPages.webview

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.buildAnnotatedString
import androidx.lifecycle.viewmodel.compose.viewModel
import co.touchlab.kermit.Logger
import com.multiplatform.webview.jsbridge.IJsMessageHandler
import com.multiplatform.webview.jsbridge.JsMessage
import com.multiplatform.webview.jsbridge.rememberWebViewJsBridge
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.WebViewNavigator
import com.multiplatform.webview.web.rememberWebViewStateWithHTMLData
import compose.icons.FeatherIcons
import compose.icons.feathericons.ArrowLeft
import ru.lavafrai.maiapp.fragments.LoadableView
import ru.lavafrai.maiapp.platform.getPlatform
import ru.lavafrai.maiapp.rootPages.main.MainPageTitle
import ru.lavafrai.maiapp.utils.toHex
import ru.lavafrai.maiapp.viewmodels.webview.WebviewViewModel


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun WebViewPage(
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onNavigateBack: () -> Unit,
    url: String,
    title: String,
) = Column(modifier = Modifier.fillMaxSize()) {
    val viewModel: WebviewViewModel = viewModel(factory = WebviewViewModel.Factory(url, title))
    val viewState by viewModel.state.collectAsState()
    val jsBridge = rememberWebViewJsBridge()

    MainPageTitle(
        titleText = { Text(title) },
        leftButton = {
            IconButton(onClick = onNavigateBack) {
                Icon(FeatherIcons.ArrowLeft, contentDescription = "back")
            }
        },
        rightButton = {

        }
    )

    val background = MaterialTheme.colorScheme.background
    LoadableView(
        viewState.page,
        retry = viewModel::refresh,
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth(),
        alignment = Alignment.Center,
    ) { page ->
        val css = buildCssStyles()
        val js = buildJsScripts()
        val builtPage = "<script>\n$js\n</script>$page\n<style>\n$css\n</style>"
        val webViewState = rememberWebViewStateWithHTMLData(data = builtPage)
        webViewState.webSettings.apply {
            isJavaScriptEnabled = true
            supportZoom = false

            iOSWebSettings.apply {
                opaque = true
                backgroundColor = background
            }

            desktopWebSettings.apply {
                transparent = false
            }
        }

        val clipboard = LocalClipboardManager.current
        LaunchedEffect(jsBridge) {
            jsBridge.register(ClipboardJsMessageHandler {
                clipboard.setText(buildAnnotatedString { append(it) })
            })
        }
        LaunchedEffect(jsBridge) {
            jsBridge.register(MapJsMessageHandler())
        }

        WebView(
            state = webViewState,
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars),
            webViewJsBridge = jsBridge,
        )
    }
}

class ClipboardJsMessageHandler(
    val copy: (String) -> Unit
): IJsMessageHandler {
    override fun handle(message: JsMessage, navigator: WebViewNavigator?, callback: (String) -> Unit) {
        Logger.i("Copy handler message: $message")
        val param = message.params
        copy(param)
    }

    override fun methodName() = "copyToClipboard"
}

class MapJsMessageHandler(): IJsMessageHandler {
    override fun handle(message: JsMessage, navigator: WebViewNavigator?, callback: (String) -> Unit) {
        Logger.i("Map handler message: $message")
        val param = message.params
        // getPlatform().openUrl("geo:$param")

        val url = if (getPlatform().name() == "Android") "geo:$param" else {
            val lat = param.split(",")[0]
            val lon = param.split(",")[1]
            "https://yandex.ru/maps/?pt=$lon,$lat&z=17"
        }
        getPlatform().openUrl(url)
    }

    override fun methodName() = "openMap"
}

@Composable
fun buildCssStyles(): String {
    val theme = MaterialTheme.colorScheme
    val css = """
        html, body {
            background-color: ${theme.background.toHex()};
            color: ${theme.onBackground.toHex()};
            --primary: ${theme.primary.toHex()};
            --on-primary: ${theme.onPrimary.toHex()};
            --secondary: ${theme.secondary.toHex()};
            --on-secondary: ${theme.onSecondary.toHex()};
            --surface: ${theme.surface.toHex()};
            --on-surface: ${theme.onSurface.toHex()};
            --error: ${theme.error.toHex()};
            --on-error: ${theme.onError.toHex()};
            --surface-variant: ${theme.surfaceVariant.toHex()};
            --on-surface-variant: ${theme.onSurfaceVariant.toHex()};
            
            font-family: 'Roboto', sans-serif;
            font-size: 16px;
            user-select: none;
        }
        
        row {
            display: flex;
            flex-direction: row;
        }
        
        column {
            display: flex;
            flex-direction: column;
            gap: attr(padding);
        }
        
        card {
            display: flex;
            flex-direction: column;
            background-color: var(--surface-variant);
            color: var(--on-surface-variant);
            border-radius: 12px;
            box-shadow: 0 2px 4px 0 rgba(0, 0, 0, 0.1);
            padding: 8px;
            gap: 8px;
        }
        
        .primary-color {
            color: var(--primary);
        }
        
        .subtitle {
            font-size: 20px;
        }
    """.trimIndent()

    return css
}

fun buildJsScripts(): String {
    val js = """
        function copy(text) {
            window.kmpJsBridge.callNative("copyToClipboard", text, function(data) {})
        }
        function openMap(text) {
            window.kmpJsBridge.callNative("openMap", text, function(data) {})
        }
    """.trimIndent()

    return js
}