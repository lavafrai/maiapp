package ru.lavafrai.maiapp.localizers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.staticCompositionLocalOf
import platform.Foundation.NSLocale
import platform.Foundation.NSUserDefaults
import platform.Foundation.preferredLanguages

actual object LocalAppLocale {
    private const val LANG_KEY = "AppleLanguages"
    // iOS keeps the app language chosen in its own settings in AppleLanguages too, so with the "system" language
    // it's removed only if the app has written it
    private const val SET_BY_APP_KEY = "maiapp.AppleLanguagesSetByApp"
    private val default = NSLocale.preferredLanguages.first() as String
    private val LocalAppLocale = staticCompositionLocalOf { default }

    actual val current: String
        @Composable
        get() = LocalAppLocale.current

    @Composable
    actual infix fun provides(value: String?): ProvidedValue<*> {
        val defaults = NSUserDefaults.standardUserDefaults
        if (value != null) {
            defaults.setObject(listOf(value), LANG_KEY)
            defaults.setBool(true, SET_BY_APP_KEY)
        } else if (defaults.boolForKey(SET_BY_APP_KEY)) {
            defaults.removeObjectForKey(LANG_KEY)
            defaults.setBool(false, SET_BY_APP_KEY)
        }
        return LocalAppLocale.provides(value ?: default)
    }
}
