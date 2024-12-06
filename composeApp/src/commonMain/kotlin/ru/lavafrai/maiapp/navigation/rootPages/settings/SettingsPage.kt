@file:OptIn(ExperimentalSharedTransitionApi::class)

package ru.lavafrai.maiapp.navigation.rootPages.settings

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import maiapp.composeapp.generated.resources.Res
import maiapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import ru.lavafrai.maiapp.data.settings.ApplicationSettings
import ru.lavafrai.maiapp.data.settings.rememberSettings
import ru.lavafrai.maiapp.fragments.settings.ThemeSelectButton
import ru.lavafrai.maiapp.platform.getPlatform
import ru.lavafrai.maiapp.theme.ThemeProvider

@Composable
fun SettingsPage() {
    val settings by rememberSettings()

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SettingsSection(title = stringResource(Res.string.appearance)) {
            ThemeSelectButton { themeId ->
                ApplicationSettings.setTheme(themeId)
            }

            val colorSchemaIds = ThemeProvider.colorSchemas.map { it.readableName() to it.id }.toMap()
            SettingsDropdownItem(
                title = stringResource(Res.string.color_scheme),
                items = ThemeProvider.colorSchemas.map { it.readableName() },
                selected = ThemeProvider.findColorSchemaById(settings.colorSchema).readableName(),
                onItemSelected = { schema ->
                    val schemaId = colorSchemaIds[schema] ?: return@SettingsDropdownItem
                    ApplicationSettings.setColorScheme(schemaId)
                }
            )
        }
    }
}
