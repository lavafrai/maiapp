package ru.lavafrai.maiapp.fragments

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import compose.icons.FeatherIcons
import compose.icons.feathericons.Copy
import compose.icons.feathericons.Repeat
import compose.icons.feathericons.Trash
import compose.icons.feathericons.Trash2
import maiapp.composeapp.generated.resources.*
import maiapp.composeapp.generated.resources.Res
import maiapp.composeapp.generated.resources.copy_stacktrace
import maiapp.composeapp.generated.resources.retry
import maiapp.composeapp.generated.resources.something_went_wrong
import org.jetbrains.compose.resources.stringResource
import ru.lavafrai.maiapp.LocalApplicationContext

@Composable
fun ErrorView(
    error: Throwable?,
    onRetry: () -> Unit,
) {
    val clipboardManager = LocalClipboardManager.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(Res.string.something_went_wrong))
        Spacer(Modifier.height(4.dp))

        TextButton(onClick = onRetry) {
            Icon(FeatherIcons.Repeat, "Retry")
            Spacer(Modifier.width(4.dp))
            Text(stringResource(Res.string.retry))
        }

        TextButton(onClick = {
                clipboardManager.setText(buildAnnotatedString {
                    append(error?.stackTraceToString())
                })
            },
            enabled = error != null) {
            Icon(FeatherIcons.Copy, "Copy stacktrace")
            Spacer(Modifier.width(4.dp))
            Text(stringResource(Res.string.copy_stacktrace))
        }

        val applicationContext = LocalApplicationContext.current
        TextButton(onClick = { applicationContext.panicCleanup() }) {
            Icon(FeatherIcons.Trash2, "Clear data and restart")
            Spacer(Modifier.width(4.dp))
            Text(stringResource(Res.string.clear_data_and_restart))
        }
    }
}