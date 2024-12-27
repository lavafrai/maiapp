@file:OptIn(ExperimentalSharedTransitionApi::class)

package ru.lavafrai.maiapp.rootPages.settings

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
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
import ru.lavafrai.maiapp.platform.getPlatform
import ru.lavafrai.maiapp.theme.ThemeProvider
import ru.lavafrai.maiapp.utils.spAsDp

@Composable
fun SettingsPage() {
    val settings by rememberSettings()

    PageColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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

        WidgetSettings()
    }
}

@Composable
fun WidgetSettings() = SettingsSection(title = stringResource(Res.string.widget)) {
    val platform = getPlatform()
    val widgetSupported = platform.supportsWidget()

    if (widgetSupported) {
        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Text(stringResource(Res.string.widget_description))
        }
    } else {
        Box(modifier = Modifier.heightIn(min = 64.dp).fillMaxWidth().padding(horizontal = 32.dp)) {
            Text(stringResource(Res.string.widget_unsupported_on_platform), modifier = Modifier.align(Alignment.Center))
        }
    }

    Button(onClick = { platform.requestWidgetCreation() }, enabled = widgetSupported, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(Res.string.add_widget))
    }
}
