package ru.lavafrai.maiapp.rootPages.settings

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.lavafrai.maiapp.fragments.AppCard

@Composable
fun SettingsSection(
    title: String,
    body: @Composable () -> Unit,
) {
    AppCard {
        Text(title, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(12.dp))
        body()
    }
}