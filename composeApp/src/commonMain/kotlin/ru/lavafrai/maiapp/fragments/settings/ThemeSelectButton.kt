package ru.lavafrai.maiapp.fragments.settings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.lavafrai.maiapp.data.settings.ApplicationSettings
import ru.lavafrai.maiapp.theme.ThemeProvider

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
            ) {
                Text(theme.readableName)
            }
        }
    }
}