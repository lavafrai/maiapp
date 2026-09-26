package ru.lavafrai.maiapp.localizers

import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

actual object LocalAppLocale {
    actual val current: String
        @Composable
        get() = Locale.getDefault().toString()

    @Composable
    actual infix fun provides(value: String?): ProvidedValue<*> {
        val configuration = LocalConfiguration.current
        val new = apply(value)
        configuration.setLocale(new)
        val resources = LocalContext.current.resources
        @Suppress("DEPRECATION")
        resources.updateConfiguration(configuration, resources.displayMetrics)
        return LocalConfiguration.provides(configuration)
    }

    /** Sets the process default locale; also for work without UI, like the widget, where [provides] isn't called */
    fun apply(value: String?): Locale {
        // Taken each time rather than remembered: Android updates it in a living process when the device language changes
        val new = if (value == null) Resources.getSystem().configuration.locales[0] else Locale.forLanguageTag(value)
        Locale.setDefault(new)
        return new
    }
}
