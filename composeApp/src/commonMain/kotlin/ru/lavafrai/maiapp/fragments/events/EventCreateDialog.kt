package ru.lavafrai.maiapp.fragments.events

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.datetime.*
import maiapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import ru.lavafrai.maiapp.LocalApplicationContext
import ru.lavafrai.maiapp.fragments.DatePickerDialog
import ru.lavafrai.maiapp.fragments.TimePickerDialog
import ru.lavafrai.maiapp.localizers.localizedGenitive
import ru.lavafrai.maiapp.models.events.Event


@Composable
fun EventCreateDialog(
    initialDate: LocalDate,
    onDismissRequest: () -> Unit,
    onEventCreated: (Event) -> Unit,
) {
    val appContext = LocalApplicationContext.current

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() }
    val scope = rememberCoroutineScope()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
    ) {
        Column(Modifier.fillMaxSize()) {
            EventCreateContent(
                initialDate = initialDate,
                onDismissRequest = onDismissRequest,
                onEventCreated = onEventCreated,
            )
        }
    }

    /*AlertDialog(
        onDismissRequest = {  },
        dismissButton = {
            TextButton(onClick = { onDismissRequest() }) {
                Text(stringResource(Res.string.cancel))
            }
        },
        confirmButton = {
            TextButton(onClick = {  }) {
                Text(stringResource(Res.string.done))
            }
        },
        text = {
            EventCreateContent(
                initialDate = initialDate,
                onDismissRequest = onDismissRequest,
                onEventCreated = onEventCreated,
            )
        }
    )*/
}

@Composable
fun EventCreateContent(
    initialDate: LocalDate,
    onDismissRequest: () -> Unit,
    onEventCreated: (Event) -> Unit,
) = Column(Modifier.padding(vertical = 16.dp)) {
    var date by remember { mutableStateOf(initialDate) }
    var startTime by remember { mutableStateOf(LocalTime(hour = 10, minute = 45)) }
    var endTime by remember { mutableStateOf(LocalTime(hour = 12, minute = 15)) }
    var eventName by remember { mutableStateOf("") }

    EventCreateDialogName(
        name = eventName,
        onNameChanged = {
            eventName = it
        }
    )

    EventCreateDialogDateTime(
        initialDate = initialDate,
        date = date,
        startTime = startTime,
        endTime = endTime,
        onDateChanged = { date = it },
        onStartTimeChanged = { startTime = it },
        onEndTimeChanged = { endTime = it },
    )
}

@Composable
fun EventCreateDialogName(
    name: String = "",
    onNameChanged: (String) -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Column(
        Modifier.padding(horizontal = 8.dp)
    ) {
        BasicTextField(
            value = name,
            onValueChange = onNameChanged,
            modifier = Modifier
                .fillMaxWidth(),
            textStyle = TextStyle(
                fontSize = 28.sp,
                textAlign = TextAlign.Start,
                color = MaterialTheme.colorScheme.onSurface
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            maxLines = 1,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                }
            ),
            decorationBox = { innerTextField ->
                Box(
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (name.isEmpty()) {
                        Text(
                            text = stringResource(Res.string.event_name),
                            fontSize = 28.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

@Composable
fun EventCreateDialogDateTime(
    initialDate: LocalDate?,
    date: LocalDate,
    startTime: LocalTime,
    endTime: LocalTime,
    onDateChanged: (LocalDate) -> Unit,
    onStartTimeChanged: (LocalTime) -> Unit,
    onEndTimeChanged: (LocalTime) -> Unit,
) {
    var startTimeExpanded by remember { mutableStateOf(false) }
    var endTimeExpanded by remember { mutableStateOf(false) }
    var dateExpanded by remember { mutableStateOf(false) }
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    val dateText = if (now.year == date.year) "${date.dayOfMonth} ${date.month.localizedGenitive()}"
    else "${date.dayOfMonth} ${date.month.localizedGenitive()} ${date.year}"
    val startTimeText = "${startTime.hour}:${startTime.minute.toString().padStart(2, '0')}"
    val endTimeText = "${endTime.hour}:${endTime.minute.toString().padStart(2, '0')}"
    val horizontalPadding = 8.dp

    Column(
        Modifier
            .padding(vertical = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(Modifier.clickable { dateExpanded = true }.padding(horizontal = horizontalPadding).fillMaxWidth()) {
            Text(dateText, style = MaterialTheme.typography.displaySmall)
        }
        Row(Modifier.padding(horizontal = horizontalPadding)) {
            CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.displayLarge) {
                Text(startTimeText, modifier = Modifier.clickable { startTimeExpanded = true })
                Text(" - ", modifier = Modifier.alpha(0.8f))
                Text(endTimeText, modifier = Modifier.clickable { endTimeExpanded = true })
            }
        }
    }

    if (startTimeExpanded) TimePickerDialog(
        onDismiss = { startTimeExpanded = false },
        onConfirm = { startTimeExpanded = false; onStartTimeChanged(it) },
        currentTime = startTime
    )
    if (endTimeExpanded) TimePickerDialog(
        onDismiss = { endTimeExpanded = false },
        onConfirm = { endTimeExpanded = false; onEndTimeChanged(it) },
        currentTime = endTime
    )
    if (dateExpanded) DatePickerDialog(
        onDismiss = { dateExpanded = false },
        onConfirm = { dateExpanded = false; onDateChanged(it) },
        currentDate = date
    )
}
