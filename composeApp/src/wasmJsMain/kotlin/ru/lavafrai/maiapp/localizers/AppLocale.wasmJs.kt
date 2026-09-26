package ru.lavafrai.maiapp.localizers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.intl.Locale

actual object LocalAppLocale {
    private val LocalAppLocale = staticCompositionLocalOf { Locale.current }

    actual val current: String
        @Composable
        get() = LocalAppLocale.current.toString()

    @Composable
    actual infix fun provides(value: String?): ProvidedValue<*> {
        updateCustomLocale(value?.replace('_', '-'))
        return LocalAppLocale.provides(Locale.current)
    }
}

// index.html makes navigator.languages return window.__customLocale when it's set
private fun updateCustomLocale(value: String?): Unit = js(
    """{
        if (window.__customLocale !== value) {
            window.__customLocale = value;
            window.dispatchEvent(new Event("languagechange"));
        }
    }"""
)
