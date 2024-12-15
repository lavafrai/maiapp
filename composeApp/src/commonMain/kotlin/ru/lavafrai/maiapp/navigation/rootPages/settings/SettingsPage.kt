@file:OptIn(ExperimentalSharedTransitionApi::class)

package ru.lavafrai.maiapp.navigation.rootPages.settings

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import maiapp.composeapp.generated.resources.Res
import maiapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import ru.lavafrai.maiapp.data.settings.ApplicationSettings
import ru.lavafrai.maiapp.data.settings.rememberSettings
import ru.lavafrai.maiapp.fragments.PageColumn
import ru.lavafrai.maiapp.fragments.settings.ThemeSelectButton
import ru.lavafrai.maiapp.theme.ThemeProvider
import ru.lavafrai.maiapp.utils.spAsDp

@Composable
fun SettingsPage() {
    val settings by rememberSettings()

    PageColumn {
        SettingsSection(title = stringResource(Res.string.appearance)) {
            ThemeSelectButton { themeId ->
                ApplicationSettings.setTheme(themeId)
            }

            // val colorSchemaIds = ThemeProvider.colorSchemas.map { it.readableName() to it.id }.toMap()
            SettingsDropdownItem(
                title = stringResource(Res.string.color_scheme),
                items = ThemeProvider.colorSchemas,
                selected = ThemeProvider.findColorSchemaById(settings.colorSchema),
                onItemSelected = { schema ->
                    val schemaId = schema.id
                    ApplicationSettings.setColorScheme(schemaId)
                },
                itemContent = {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(it.buildColorScheme(ThemeProvider.findThemeById(settings.theme)).primary)
                            .size(18.spAsDp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(it.readableName(), fontSize = 18.sp)
                }
            )
        }
    }
}
