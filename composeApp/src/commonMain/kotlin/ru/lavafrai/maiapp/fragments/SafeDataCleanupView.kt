package ru.lavafrai.maiapp.fragments

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import compose.icons.FeatherIcons
import compose.icons.feathericons.AlertOctagon
import maiapp.composeapp.generated.resources.Res
import maiapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import ru.lavafrai.maiapp.LocalApplicationContext

@Composable
fun SafeDataCleanupView(
    onNavigateBack: () -> Unit,
    onClean: () -> Unit,
) {
    val appContext = LocalApplicationContext.current

    Dialog(onDismissRequest = onNavigateBack) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            shape = MaterialTheme.shapes.large,
        ) {
            Column(
                Modifier
                    .padding(32.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    FeatherIcons.AlertOctagon,
                    "warning!"
                )

                Text(
                    stringResource(Res.string.safe_data_cleanup_warning),
                    style = MaterialTheme.typography.headlineSmall,
                )

                Text(
                    stringResource(Res.string.safe_data_cleanup_warning_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                )

                Row(
                    Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    TextButton(
                        onClick = onClean,
                    ) {
                        Text(stringResource(Res.string.confirm))
                    }

                    Button(onClick = onNavigateBack) {
                        Text(stringResource(Res.string.cancel))
                    }
                }
            }
        }
    }
}