package ru.lavafrai.maiapp.rootPages.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
        Text(title, style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        body()
    }
}