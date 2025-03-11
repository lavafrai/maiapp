package ru.lavafrai.maiapp.fragments.hypertext

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material.RichText
import ru.lavafrai.maiapp.theme.LinkColor
import ru.lavafrai.maiapp.utils.UrlHandler

@Composable
fun Hypertext(
    hypertext: String,
    baseLinkPath: String = "https://mai-exler.ru",
    color: Color = LocalContentColor.current,
) {
    val state = rememberRichTextState()
    val appUrlHandler = UrlHandler.create()
    val uriHandler = object : UriHandler {
        override fun openUri(uri: String) {
            val fullUrl = if (uri.startsWith("http")) uri else "$baseLinkPath$uri"
            appUrlHandler.openUrl(fullUrl)
        }
    }

    remember(state) {
        state.config.linkColor = LinkColor
    }

    remember(hypertext) {
        val processedHypertext = hypertext
            .replace("&#x97;", "-")
            .replace("\u0097", "-")
            .replace("&#x96;", "-")
            .replace("\u0096;", "-")
            .replace("&#x85;", "…")
            .replace("\u0085;", "…")
            .replace("&#x91;", "‘")
            .replace("\u0091;", "‘")
            .replace("&#x92;", "’")
            .replace("\u0092;", "’")
            .replace("&#x93;", "“")
            .replace("\u0093;", "“")
            .replace("&#x94;", "”")
            .replace("\u0094;", "”")
        state.setHtml(processedHypertext)
    }

    CompositionLocalProvider(LocalUriHandler provides uriHandler) {
        RichText(
            state = state,
            style = MaterialTheme.typography.bodyLarge,
            color = color,
        )
    }
}