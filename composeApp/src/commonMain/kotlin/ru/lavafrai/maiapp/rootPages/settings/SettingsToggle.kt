package ru.lavafrai.maiapp.rootPages.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun SettingsToggle(
    description: @Composable () -> Unit = {},
    toggled: Boolean,
    onToggle: (Boolean) -> Unit,
    enabled: Boolean = true,
) = Row(
    modifier = Modifier
        .fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
) {
    CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onBackground)) {
        description()
    }
    Row(
        modifier = Modifier
            .weight(1f),
        horizontalArrangement = Arrangement.End,
    ){
        Switch(toggled, onToggle)
    }
}