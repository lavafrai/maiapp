package ru.lavafrai.maiapp.fragments

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Dialog

@Composable
fun UpdateInfoDialog(
    lastVersion: String?,
    currentVersion: String,
    onDismissRequest: () -> Unit,
    onOkay: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
    ) {
        AppCard {
            Text("Обновление")
            Text("${lastVersion ?: "Неизвестно"} -> $currentVersion")
        }
    }
}