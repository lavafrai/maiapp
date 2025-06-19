package ru.lavafrai.maiapp.fragments.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import compose.icons.FeatherIcons
import compose.icons.feathericons.ChevronDown
import ru.lavafrai.maiapp.data.settings.ApplicationSettings
import ru.lavafrai.maiapp.theme.ThemeProvider
import kotlin.math.max

@Composable
fun ThemeSelectButton(
    setTheme: (String) -> Unit,
) {
    val settings = ApplicationSettings.state.value
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier.fillMaxWidth()
    ) {
        ThemeProvider.themes.forEachIndexed { index, theme ->
            SegmentedButton(
                selected = settings.theme == theme.id,
                onClick = {
                    setTheme(theme.id)
                },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = ThemeProvider.themes.size),
                colors = SegmentedButtonDefaults.colors(
                    inactiveContainerColor = MaterialTheme.colorScheme.background,
                ),
            ) {
                Text(theme.readableName(), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}