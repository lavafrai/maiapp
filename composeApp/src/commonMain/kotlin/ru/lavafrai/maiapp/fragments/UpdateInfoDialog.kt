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
import compose.icons.feathericons.ArrowRight
import maiapp.composeapp.generated.resources.*
import maiapp.composeapp.generated.resources.Res
import maiapp.composeapp.generated.resources.update
import maiapp.composeapp.generated.resources.close
import maiapp.composeapp.generated.resources.update_dialog_gratitude
import maiapp.composeapp.generated.resources.update_dialog_scam
import org.jetbrains.compose.resources.stringResource
import ru.lavafrai.maiapp.LocalApplicationContext

@Composable
fun UpdateInfoDialog(
    lastVersion: String?,
    currentVersion: String,
    onDismissRequest: () -> Unit,
    onOkay: () -> Unit,
) {
    val appContext = LocalApplicationContext.current

    Dialog(
        onDismissRequest = onDismissRequest,
    ) {
        AppCard {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                //Text("Обновление", style = MaterialTheme.typography.titleLarge)
                Text(stringResource(Res.string.update), style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 16.dp),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        if (lastVersion == null) CrossedChip {  }
                        else CrossedChip { Text(lastVersion) }
                    }
                    Icon(
                        imageVector = FeatherIcons.ArrowRight,
                        contentDescription = "Информация о версии",
                    )
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        SimpleChip { Text(currentVersion) }
                    }
                }
                Spacer(Modifier.height(16.dp))

                Text(stringResource(Res.string.update_dialog_gratitude), style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))
                Text(stringResource(Res.string.update_dialog_scam), style = MaterialTheme.typography.bodyMedium)

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    TextButton(
                        onClick = { appContext.openDonations() ; onDismissRequest() },
                        modifier = Modifier.padding(top = 16.dp),
                    ) {
                        Text(stringResource(Res.string.do_support))
                    }

                    Button(
                        onClick = onDismissRequest,
                        modifier = Modifier.padding(top = 16.dp),
                    ) {
                        Text(stringResource(Res.string.close))
                    }
                }
            }
        }
    }
}