package ru.lavafrai.maiapp.fragments.hypertext

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material.RichText
import ru.lavafrai.maiapp.platform.getPlatform
import ru.lavafrai.maiapp.theme.LinkColor
import ru.lavafrai.maiapp.utils.UrlHandler

@Composable
fun Hypertext(
    hypertext: String,
    baseLinkPath: String = "https://mai-exler.ru",
) {
    val state = rememberRichTextState()
    val appUrlHandler = UrlHandler.create()
    val uriHandler = object : UriHandler {
        override fun openUri(uri: String) {
            val fullUrl = if (uri.startsWith("http")) uri else "$baseLinkPath$uri"
            appUrlHandler.openUrl(fullUrl)
        }
    }
    LaunchedEffect(state) {
        state.config.linkColor = LinkColor
    }

    LaunchedEffect(hypertext) {
        val processedHypertext = hypertext
            .replace("&#x97;", "-")
            .replace("&#x96;", "-")
            .replace("&#x85;", "…")
            .replace("&#x91;", "‘")
            .replace("&#x92;", "’")
            .replace("&#x93;", "“")
            .replace("&#x94;", "”")
        state.setHtml(processedHypertext)
    }

    CompositionLocalProvider(LocalUriHandler provides uriHandler) {
        RichText(
            state = state,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}