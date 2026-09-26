package ru.lavafrai.maiapp.localizers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.key
import androidx.compose.ui.text.intl.Locale
import ru.lavafrai.maiapp.data.settings.ApplicationSettings
import ru.lavafrai.maiapp.data.settings.rememberSettings

/** Languages the app is translated to, as BCP 47 tags; null in settings means the system language */
val appLanguages = listOf("ru", "en")

/**
 * The language resources are taken in. Compose resources take it from the platform locale, so choosing a language
 * means changing that; see https://kotlinlang.org/docs/multiplatform/compose-resource-environment.html
 */
expect object LocalAppLocale {
    val current: String
        @Composable
        get

    @Composable
    infix fun provides(value: String?): ProvidedValue<*>
}

/**
 * The app locale for our server, like "ru_RU" or just "en": the language chosen in the app or the system one, with
 * the region when the platform locale has it (a language chosen in the app usually replaces it with none).
 * The server only needs the language from it
 */
fun appLocaleTag(): String {
    val system = Locale.current
    val language = ApplicationSettings.state.value.language ?: system.language
    return if (system.region.isNotEmpty()) "${language}_${system.region}" else language
}

/** Applies the language chosen in settings; the content is recreated when it changes */
@Composable
fun AppEnvironment(content: @Composable () -> Unit) {
    val settings = rememberSettings()
    // Only the language: providing it has side effects (e.g. a resources configuration update on Android)
    val language by remember { derivedStateOf { settings.value.language } }
    CompositionLocalProvider(LocalAppLocale provides language) {
        key(language) {
            content()
        }
    }
}
