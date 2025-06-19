package ru.lavafrai.maiapp.fragments

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import kotlinx.datetime.LocalDate
import maiapp.composeapp.generated.resources.Res
import maiapp.composeapp.generated.resources.cancel
import maiapp.composeapp.generated.resources.done
import org.jetbrains.compose.resources.stringResource


@Composable
fun DatePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit,
    currentDate: LocalDate,
) {
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = currentDate.toEpochDays().toLong().times(3600 * 24 * 1000))

    androidx.compose.material3.DatePickerDialog(
        onDismissRequest = onDismiss,
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(stringResource(Res.string.cancel))
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (datePickerState.selectedDateMillis != null) onConfirm(LocalDate.fromEpochDays(datePickerState.selectedDateMillis!!.div(3600 * 24 * 1000).toInt()))
                else onConfirm(currentDate)
            }) {
                Text(stringResource(Res.string.done))
            }
        },
        content = {
            androidx.compose.material3.DatePicker(
                state = datePickerState,
            )
        }
    )
}