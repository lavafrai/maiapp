@file:OptIn(ExperimentalSharedTransitionApi::class)

package ru.lavafrai.maiapp.rootPages.settings

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import compose.icons.FeatherIcons
import compose.icons.LineAwesomeIcons
import compose.icons.feathericons.DollarSign
import compose.icons.feathericons.Edit3
import compose.icons.feathericons.Github
import compose.icons.lineawesomeicons.HeartSolid
import compose.icons.lineawesomeicons.Telegram
import ru.lavafrai.maiapp.utils.asDp
import ru.lavafrai.maiapp.localizers.appLanguages
import maiapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import kotlinx.coroutines.delay
import ru.lavafrai.maiapp.BuildConfig
import ru.lavafrai.maiapp.LocalApplicationContext
import ru.lavafrai.maiapp.data.Loadable
import ru.lavafrai.maiapp.data.settings.ApplicationSettings
import ru.lavafrai.maiapp.data.settings.rememberSettings
import ru.lavafrai.maiapp.fragments.PageColumn
import ru.lavafrai.maiapp.fragments.settings.ThemeSelectButton
import ru.lavafrai.maiapp.models.schedule.Schedule
import ru.lavafrai.maiapp.platform.getPlatform
import ru.lavafrai.maiapp.theme.ThemeProvider
import ru.lavafrai.maiapp.utils.formatBinarySize
import ru.lavafrai.maiapp.utils.getStorageUsage
import ru.lavafrai.maiapp.utils.spAsDp

@Composable
fun SettingsPage(
    schedule: Loadable<Schedule>,
) {
    val settings by rememberSettings()

    PageColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Spacer(Modifier.height(8.dp))

        SchedulesSettings(schedule = schedule)

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
                    Text(it.readableName(), fontSize = 18.sp)
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(it.buildColorScheme(ThemeProvider.findThemeById(settings.theme)).primary)
                            .size(18.spAsDp)
                    )
                },
                selectedContent = {
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

            SettingsDropdownItem(
                title = stringResource(Res.string.language),
                items = listOf(null) + appLanguages,
                selected = settings.language,
                onItemSelected = {
                    ApplicationSettings.setLanguage(it)
                    getPlatform().onLanguageChanged()
                },
                itemContent = { Text(languageName(it), fontSize = 18.sp) },
            )
        }

        WidgetSettings()

        // DataSettings()

        OpenSourceInfo()

        DataCleanButton()

        if (settings.developerMode) DeveloperSettings()

        SettingsCopyright()
    }
}

/** Languages are named in themselves, as users look for their own */
@Composable
private fun languageName(language: String?): String = when (language) {
    null -> stringResource(Res.string.language_system)
    "ru" -> "Русский"
    "en" -> "English"
    else -> language
}

@Composable
fun OpenSourceInfo() = SettingsSection(stringResource(Res.string.information)) {
    val platform = getPlatform()
    val appContext = LocalApplicationContext.current

    Text(
        stringResource(Res.string.settings_info),
        style = LocalTextStyle.current.copy(
            lineBreak = LineBreak.Paragraph,
            hyphens = Hyphens.Auto
        )
    )
    Spacer(Modifier.height(8.dp))
    Button(onClick = { platform.openThanks() }, shapes = ButtonDefaults.shapes(), Modifier.fillMaxWidth()) {
        Icon(FeatherIcons.Edit3, contentDescription = null)
        Spacer(Modifier.width(4.dp))
        Text(stringResource(Res.string.open_thanks))
    }

    Button(onClick = { platform.openGitHub() }, shapes = ButtonDefaults.shapes(), Modifier.fillMaxWidth()) {
        Icon(FeatherIcons.Github, contentDescription = null)
        Spacer(Modifier.width(4.dp))
        Text(stringResource(Res.string.open_github))
    }

    Button(onClick = { appContext.openDonations() }, shapes = ButtonDefaults.shapes(), Modifier.fillMaxWidth()) {
        Icon(FeatherIcons.DollarSign, contentDescription = null)
        Spacer(Modifier.width(4.dp))
        Text(stringResource(Res.string.support_project))
    }

    Button(onClick = { appContext.openTelegram() }, shapes = ButtonDefaults.shapes(), Modifier.fillMaxWidth()) {
        Icon(LineAwesomeIcons.Telegram, contentDescription = null)
        Spacer(Modifier.width(4.dp))
        Text(stringResource(Res.string.open_telegram))
    }

    Spacer(Modifier.height(8.dp))
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 2.dp)
                .clip(MaterialTheme.shapes.small)
                .clickable { appContext.openUrl("https://lavafrai.ru/") }
        ) {
            // An icon, not "♥": fonts of the web version have no such glyph and draw a box instead
            Text("With ")
            Icon(LineAwesomeIcons.HeartSolid, contentDescription = "love", modifier = Modifier.size(LocalTextStyle.current.fontSize.asDp))
            Text(" by. lava_frai")
        }
    }
}

@Composable
fun SettingsCopyright() {
    var versionTaps by remember { mutableIntStateOf(0) }
    Column(
        Modifier.fillMaxWidth().padding(top = 16.dp).alpha(0.5f),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("MAI app by. lava_frai")
        Text(
            "Build: ${BuildConfig.VERSION_NAME}@${getPlatform().name()}",
            modifier = Modifier.clickable {
                versionTaps++
                if (versionTaps >= 7) {
                    ApplicationSettings.setDeveloperMode(true)
                    versionTaps = 0
                }
            },
        )
    }
}

@Composable
private fun DeveloperSettings() {
    val clipboard = LocalClipboardManager.current
    var showDisableConfirmation by remember { mutableStateOf(false) }
    var deviceId by remember { mutableStateOf<String?>(null) }
    var deviceIdLoaded by remember { mutableStateOf(false) }
    var copied by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        deviceId = getPlatform().appMetricaDeviceId()
        deviceIdLoaded = true
    }

    LaunchedEffect(copied) {
        if (copied) {
            delay(2_000)
            copied = false
        }
    }

    SettingsSection(title = stringResource(Res.string.developer_settings)) {
        Text(stringResource(Res.string.appmetrica_device_id))
        Text(
            when {
                !deviceIdLoaded -> stringResource(Res.string.loading)
                deviceId == null -> stringResource(Res.string.unavailable)
                else -> deviceId!!
            },
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.clickable(enabled = deviceId != null) {
                clipboard.setText(AnnotatedString(deviceId!!))
                copied = true
            },
        )
        if (copied) Text(stringResource(Res.string.copied_to_clipboard))
        Spacer(Modifier.height(12.dp))
        Button(onClick = { error("Developer test crash") }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(Res.string.crash_app))
        }
        Button(onClick = { showDisableConfirmation = true }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(Res.string.disable_developer_mode))
        }
    }

    if (showDisableConfirmation) {
        AlertDialog(
            onDismissRequest = { showDisableConfirmation = false },
            title = { Text(stringResource(Res.string.disable_developer_mode)) },
            text = { Text(stringResource(Res.string.disable_developer_mode_confirmation)) },
            confirmButton = {
                TextButton(onClick = {
                    ApplicationSettings.setDeveloperMode(false)
                    showDisableConfirmation = false
                }) { Text(stringResource(Res.string.disable)) }
            },
            dismissButton = {
                TextButton(onClick = { showDisableConfirmation = false }) {
                    Text(stringResource(Res.string.cancel))
                }
            },
        )
    }
}

@Composable
fun DataSettings() {
    val storageUsage = getPlatform().storage().getStorageUsage()

    SettingsSection(title = stringResource(Res.string.data)) {
        Row {
            Text("${stringResource(Res.string.storage_usage)}: ${storageUsage.formatBinarySize()}")
        }
    }
}

@Composable
fun WidgetSettings() = SettingsSection(title = stringResource(Res.string.widget)) {
    val platform = getPlatform()
    val widgetSupported = platform.supportsWidget()

    if (widgetSupported) {
        Box(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
            Text(stringResource(Res.string.widget_description))
        }
    } else {
        Box(modifier = Modifier.heightIn(min = 64.dp).fillMaxWidth().padding(horizontal = 32.dp)) {
            Text(stringResource(Res.string.widget_unsupported_on_platform), modifier = Modifier.align(Alignment.Center))
        }
    }

    Button(onClick = { platform.requestWidgetCreation() }, shapes = ButtonDefaults.shapes(), enabled = widgetSupported, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(Res.string.add_widget))
    }
}

@Composable
fun DataCleanButton() {
    val appContext = LocalApplicationContext.current

    Button(
        modifier = Modifier.fillMaxWidth(),
        onClick = { appContext.requestSafeDataClean() },
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = MaterialTheme.colorScheme.onError),
        shapes = ButtonDefaults.shapes()
    ) {
        Text(stringResource(Res.string.clear_application_data))
    }
}
