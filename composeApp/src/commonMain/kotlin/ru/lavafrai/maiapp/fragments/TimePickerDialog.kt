package ru.lavafrai.maiapp.fragments

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import kotlinx.datetime.LocalTime
import maiapp.composeapp.generated.resources.Res
import maiapp.composeapp.generated.resources.cancel
import maiapp.composeapp.generated.resources.done
import org.jetbrains.compose.resources.stringResource


@Composable
fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit,
    currentTime: LocalTime,
) {
    val timePickerState = rememberTimePickerState(
        initialHour = currentTime.hour,
        initialMinute = currentTime.minute,
        is24Hour = true,
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(stringResource(Res.string.cancel))
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(LocalTime(timePickerState.hour, timePickerState.minute)) }) {
                Text(stringResource(Res.string.done))
            }
        },
        text = {
            TimePicker(
                state = timePickerState,
            )
        }
    )
}