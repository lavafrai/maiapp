package ru.lavafrai.maiapp.fragments.events

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.touchlab.kermit.Logger
import compose.icons.FeatherIcons
import compose.icons.feathericons.MapPin
import compose.icons.feathericons.PlusCircle
import compose.icons.feathericons.User
import compose.icons.feathericons.X
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import maiapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import ru.lavafrai.maiapp.LocalApplicationContext
import ru.lavafrai.maiapp.data.repositories.ExlerRepository
import ru.lavafrai.maiapp.data.repositories.ScheduleRepository
import ru.lavafrai.maiapp.data.settings.ApplicationSettings
import ru.lavafrai.maiapp.fragments.DatePickerDialog
import ru.lavafrai.maiapp.fragments.TimePickerDialog
import ru.lavafrai.maiapp.fragments.schedule.SelectablePairNumber
import ru.lavafrai.maiapp.localizers.localized
import ru.lavafrai.maiapp.localizers.localizedBeforeTime
import ru.lavafrai.maiapp.localizers.localizedGenitive
import ru.lavafrai.maiapp.models.events.EventType
import ru.lavafrai.maiapp.models.events.SimpleEvent
import ru.lavafrai.maiapp.models.events.SimpleEventPeriod
import ru.lavafrai.maiapp.models.schedule.GroupName
import ru.lavafrai.maiapp.utils.PairTimeHelper
import kotlin.uuid.Uuid


@Composable
fun EventCreateDialog(
    initialDate: LocalDate,
    onDismissRequest: () -> Unit,
    onEventCreated: (SimpleEvent) -> Unit,
    scheduleName: String
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
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
            EventCreateContent(
                initialDate = initialDate,
                onDismissRequest = onDismissRequest,
                onRequestSoftDismiss = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    scope.launch {
                        sheetState.hide()
                        onDismissRequest()
                    }
                },
                onEventCreated = onEventCreated,
                scheduleName = scheduleName
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
    onRequestSoftDismiss: () -> Unit,
    onEventCreated: (SimpleEvent) -> Unit,
    scheduleName: String,
) = Column(Modifier.padding(vertical = 16.dp)) {
    var date by remember { mutableStateOf(initialDate) }
    var endDate by remember { mutableStateOf(initialDate + DatePeriod(months = 1)) }
    var startTime by remember { mutableStateOf(LocalTime(hour = 10, minute = 45)) }
    var endTime by remember { mutableStateOf(LocalTime(hour = 12, minute = 15)) }
    var eventName by remember { mutableStateOf("") }
    var eventNameError by remember { mutableStateOf(false) }
    var teachers by remember { mutableStateOf(emptyList<String>()) }
    var rooms by remember { mutableStateOf(emptyList<String>()) }
    var eventType by remember { mutableStateOf(EventType.Other) }
    var period by remember { mutableStateOf(SimpleEventPeriod.Single) }

    val swapTimesIfRequired = {
        if (startTime > endTime) {
            val temp = startTime
            startTime = endTime
            endTime = temp
        }
    }
    val swapDatesIfRequired = {
        if (date > endDate) {
            val temp = date
            date = endDate
            endDate = temp
        }
    }

    EventCreateDialogName(
        name = eventName,
        onNameChanged = {
            eventName = it

        },
        error = eventNameError,
    )

    EventTypeSelector(
        typeSelected = eventType,
        onTypeSelected = {
            eventType = it
        },
    )

    EventPeriodSelector(
        periodSelected = period,
        onPeriodSelected = { selectedPeriod ->
            period = selectedPeriod
        },
        modifier = Modifier.padding(top = 8.dp)
    )

    EventCreateDialogDateTime(
        period = period,
        initialDate = initialDate,
        date = date,
        endDate = endDate,
        startTime = startTime,
        endTime = endTime,
        onDateRangeChanged = { start, end ->
            date = start
            endDate = end
            swapDatesIfRequired()
        },
        onStartTimeChanged = { startTime = it; swapTimesIfRequired() },
        onEndTimeChanged = { endTime = it; swapTimesIfRequired() },
        onTimeRangeChanged = { start, end ->
            startTime = start
            endTime = end
            swapTimesIfRequired()
        }
    )

    HorizontalDivider(Modifier.padding(bottom = 16.dp).padding(horizontal = 8.dp))

    EventCreateDialogTeachers(
        teachers = teachers,
        onTeachersChanged = { teachers = it }
    )

    EventCreateDialogRooms(
        rooms = rooms,
        onRoomsChanged = { rooms = it }
    )

    // Build the event object
    EventCreateDialogButtons(
        scheduleName = scheduleName,
        onDismissRequest = onRequestSoftDismiss,
        onEventCreateRequest = {
            val cleanEventName = eventName.trim()
            if (cleanEventName.isEmpty()) {
                eventNameError = true
                Logger.w("Event name is empty")
                return@EventCreateDialogButtons
            } else {
                eventNameError = false
            }

            val builtEvent = SimpleEvent(
                name = eventName,
                date = date,
                endDate = if (period == SimpleEventPeriod.Single) null else endDate,
                startTime = startTime,
                endTime = endTime,
                room = rooms,
                teachers = teachers,
                eventType = eventType,
                period = period,
                _uuid = Uuid.NIL
            )
            Logger.d("Creating event: $builtEvent")
            onEventCreated(builtEvent)
        }
    )
}

@Composable
fun EventTypeSelector(
    typeSelected: EventType,
    onTypeSelected: (EventType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier)
        EventType.entries.forEach { type ->
            val isSelected = typeSelected == type
            InputChip(
                label = { Text(type.localized()) },
                onClick = { onTypeSelected(type) },
                selected = isSelected,
            )
        }
        Spacer(Modifier)
    }
}

@Composable
fun EventPeriodSelector(
    periodSelected: SimpleEventPeriod,
    onPeriodSelected: (SimpleEventPeriod) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier)
        SimpleEventPeriod.entries.forEach { period ->
            val isSelected = periodSelected == period
            InputChip(
                label = { Text(period.localized()) },
                onClick = { onPeriodSelected(period) },
                selected = isSelected,
            )
        }
        Spacer(Modifier)
    }
}

@Composable
fun EventCreateDialogName(
    name: String = "",
    onNameChanged: (String) -> Unit,
    error: Boolean,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val placeHolderColor = animateColorAsState(
        targetValue = if (error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        label = "placeholderColor"
    )
    LaunchedEffect(error) {
        if (error) { onNameChanged(name.trim()) }
    }

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
                            color = placeHolderColor.value,
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
    period: SimpleEventPeriod,
    initialDate: LocalDate?,
    date: LocalDate,
    endDate: LocalDate,
    startTime: LocalTime,
    endTime: LocalTime,
    onDateRangeChanged: (LocalDate, LocalDate) -> Unit,
    onStartTimeChanged: (LocalTime) -> Unit,
    onEndTimeChanged: (LocalTime) -> Unit,
    onTimeRangeChanged: (LocalTime, LocalTime) -> Unit,
) {
    var startTimeExpanded by remember { mutableStateOf(false) }
    var endTimeExpanded by remember { mutableStateOf(false) }
    var dateExpanded by remember { mutableStateOf(false) }
    var endDateExpanded by remember { mutableStateOf(false) }
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    val dateText = if (now.year == date.year) "${date.dayOfMonth} ${date.month.localizedGenitive()}"
    else "${date.dayOfMonth} ${date.month.localizedGenitive()} ${date.year}"

    val endDateText = if (now.year == date.year) "${endDate.dayOfMonth} ${endDate.month.localizedGenitive()}"
    else "${endDate.dayOfMonth} ${endDate.month.localizedGenitive()} ${endDate.year}"

    val startTimeText = "${startTime.hour}:${startTime.minute.toString().padStart(2, '0')}"
    val endTimeText = "${endTime.hour}:${endTime.minute.toString().padStart(2, '0')}"
    val horizontalPadding = 8.dp

    Column(
        Modifier
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Date selector
        Row(
            Modifier
                .padding(horizontal = horizontalPadding)
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 8.dp)
        ) {
            Column {
                if (period != SimpleEventPeriod.Single) {
                    Text(
                        period.localizedBeforeTime(),
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
                Text(
                    dateText,
                    style = MaterialTheme.typography.displaySmall,
                    modifier = Modifier.clickable { dateExpanded = true }
                )
                if (period != SimpleEventPeriod.Single) {
                    Text(
                        "по",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            lineHeight = 18.sp
                        ),
                        modifier = Modifier
                    )
                    Text(
                        endDateText,
                        style = MaterialTheme.typography.displaySmall,
                        modifier = Modifier.clickable { endDateExpanded = true }
                    )
                }
            }
        }

        HorizontalDivider(Modifier.padding(bottom = 16.dp).padding(horizontal = 8.dp))
        // Time selector
        Row(
            Modifier
                .padding(horizontal = horizontalPadding)
                .horizontalScroll(rememberScrollState())
        ) {
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
        onConfirm = {
            dateExpanded = false; onDateRangeChanged(
            it,
            if (period == SimpleEventPeriod.Single) it + DatePeriod(days = 1) else endDate
        )
        },
        currentDate = date
    )
    if (endDateExpanded) DatePickerDialog(
        onDismiss = { endDateExpanded = false },
        onConfirm = {
            endDateExpanded = false; onDateRangeChanged(date, it)
        },
        currentDate = endDate
    )

    ByPairTimeSelector(
        startTime = startTime,
        endTime = endTime,
        onTimeRangeChanged = onTimeRangeChanged,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    )
}

@Composable
fun ByPairTimeSelector(
    startTime: LocalTime,
    endTime: LocalTime,
    onTimeRangeChanged: (LocalTime, LocalTime) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedPairs = remember(startTime, endTime) { PairTimeHelper.getPairsInRange(startTime, endTime) }

    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Spacer(Modifier.width(4.dp))

        repeat(PairTimeHelper.pairCount) {
            val pairNumber = it + 1
            val pairSelected = pairNumber in selectedPairs

            SelectablePairNumber(
                text = pairNumber.toString(),
                selected = pairSelected,
                onSelectionChange = {
                    if (!pairSelected) {
                        if (selectedPairs.isEmpty()) {
                            val newStartTime = PairTimeHelper.getPairTime(pairNumber).first
                            val newEndTime = PairTimeHelper.getPairTime(pairNumber).second
                            onTimeRangeChanged(newStartTime, newEndTime)
                            return@SelectablePairNumber
                        }
                        when (pairNumber) {
                            selectedPairs.min() - 1 -> {
                                val newStartTime = PairTimeHelper.getPairTime(pairNumber).first
                                val newEndTime = PairTimeHelper.getPairTime(selectedPairs.max()).second
                                onTimeRangeChanged(newStartTime, newEndTime)
                            }

                            selectedPairs.max() + 1 -> {
                                val newStartTime = PairTimeHelper.getPairTime(selectedPairs.min()).first
                                val newEndTime = PairTimeHelper.getPairTime(pairNumber).second
                                onTimeRangeChanged(newStartTime, newEndTime)
                            }

                            else -> {
                                val newStartTime = PairTimeHelper.getPairTime(pairNumber).first
                                val newEndTime = PairTimeHelper.getPairTime(pairNumber).second
                                onTimeRangeChanged(newStartTime, newEndTime)
                            }
                        }
                    } else {
                        if (pairNumber in selectedPairs) {
                            val newSelectedPairs = selectedPairs - pairNumber
                            if (newSelectedPairs.isNotEmpty()) {
                                val newStartTime = PairTimeHelper.getPairTime(newSelectedPairs.min()).first
                                val newEndTime = PairTimeHelper.getPairTime(newSelectedPairs.max()).second
                                onTimeRangeChanged(newStartTime, newEndTime)
                            }
                        }
                    }
                }
            )
        }

        Spacer(Modifier.width(4.dp))
    }
}

@Composable
fun EventCreateDialogRooms(
    rooms: List<String>,
    onRoomsChanged: (List<String>) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var roomPickerOpened by remember { mutableStateOf(false) }

    Column(Modifier) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(FeatherIcons.MapPin, contentDescription = null)
                Text(
                    stringResource(Res.string.auditorium),
                    modifier = Modifier.padding(start = 8.dp),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            IconButton(onClick = {
                roomPickerOpened = true
            }, enabled = rooms.size < 3) {
                Icon(FeatherIcons.PlusCircle, contentDescription = null)
            }
        }

        Row(Modifier.horizontalScroll(rememberScrollState())) {
            Spacer(Modifier.width(8.dp))
            rooms.forEach { room ->
                InputChip(
                    label = { Text(room) },
                    trailingIcon = { Icon(FeatherIcons.X, null, modifier = Modifier.size(18.dp)) },
                    onClick = {
                        onRoomsChanged(rooms - room)
                    },
                    modifier = Modifier.padding(end = 4.dp),
                    selected = false
                )
            }
            Spacer(Modifier.width(8.dp))
        }
    }

    if (roomPickerOpened) {
        RoomPickerDialog(
            onDismissRequest = {
                roomPickerOpened = false
            },
            onRoomPicked = { pickedRoom ->
                if (pickedRoom.isNotBlank()) {
                    onRoomsChanged(rooms + pickedRoom)
                }
                roomPickerOpened = false
            }
        )
    }
}

@Composable
fun RoomPickerDialog(
    onDismissRequest: () -> Unit,
    onRoomPicked: (String) -> Unit,
) {
    var pickedRoom by remember { mutableStateOf("") }
    var cachedRoomPredictions by remember { mutableStateOf(null as List<String>?) }
    val predictions = remember(cachedRoomPredictions, pickedRoom) {
        cachedRoomPredictions?.filter { it.contains(pickedRoom, ignoreCase = true) }?.sorted() ?: emptyList()
    }

    LaunchedEffect(Unit) {
        val scheduleRepository = ScheduleRepository()
        val settings = ApplicationSettings.getCurrent()

        val scheduleRooms = scheduleRepository.getScheduleFromCacheOrNull(settings.selectedSchedule ?: GroupName(""))
            ?.days
            ?.flatMap { it.lessons }
            ?.flatMap { it.rooms }
            ?.map { it.name }
            ?.distinct() ?: emptyList<String>().also { Logger.w("Failed to fetch schedule rooms from cache") }
        cachedRoomPredictions = scheduleRooms.filter { it.isNotBlank() }
    }

    AlertDialog(
        confirmButton = {
            Button(onClick = { onRoomPicked(pickedRoom) }) { Text(stringResource(Res.string.add)) }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text(stringResource(Res.string.cancel)) }
        },
        onDismissRequest = onDismissRequest,
        text = {
            Column {
                OutlinedTextField(
                    value = pickedRoom,
                    onValueChange = { pickedRoom = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(Res.string.auditorium) + "…") },
                    singleLine = true,
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            onRoomPicked(pickedRoom)
                            onDismissRequest()
                        }
                    ),
                )

                LazyColumn(Modifier.fillMaxWidth().heightIn(max = 200.dp)) {
                    items(predictions) {
                        Text(
                            it,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    pickedRoom = it
                                    onRoomPicked(it)
                                    onDismissRequest()
                                }
                                .padding(8.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Start
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun EventCreateDialogTeachers(
    teachers: List<String>,
    onTeachersChanged: (List<String>) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var teacherPickerOpened by remember { mutableStateOf(false) }

    Column(Modifier) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(FeatherIcons.User, contentDescription = null)
                Text(
                    stringResource(Res.string.teacher),
                    modifier = Modifier.padding(start = 8.dp),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            IconButton(onClick = {
                teacherPickerOpened = true
            }, enabled = teachers.size < 3) {
                Icon(FeatherIcons.PlusCircle, contentDescription = null)
            }
        }

        Row(Modifier.horizontalScroll(rememberScrollState())) {
            Spacer(Modifier.width(8.dp))
            teachers.forEach { teacher ->
                InputChip(
                    label = { Text(teacher) },
                    trailingIcon = { Icon(FeatherIcons.X, null, modifier = Modifier.size(18.dp)) },
                    onClick = {
                        onTeachersChanged(teachers - teacher)
                    },
                    modifier = Modifier.padding(end = 4.dp),
                    selected = false
                )
            }
            Spacer(Modifier.width(8.dp))
        }
    }

    if (teacherPickerOpened) {
        TeacherPickerDialog(
            onDismissRequest = {
                teacherPickerOpened = false
            },
            onTeacherPicked = { pickedTeacher ->
                if (pickedTeacher.isNotBlank()) {
                    onTeachersChanged(teachers + pickedTeacher)
                }
                teacherPickerOpened = false
            }
        )
    }
}

@Composable
fun TeacherPickerDialog(
    onDismissRequest: () -> Unit,
    onTeacherPicked: (String) -> Unit,
) {
    var pickedTeacher by remember { mutableStateOf("") }
    var cachedTeacherPredictions by remember { mutableStateOf(null as List<String>?) }
    val predictions = remember(cachedTeacherPredictions, pickedTeacher) {
        cachedTeacherPredictions?.filter { it.contains(pickedTeacher, ignoreCase = true) }?.sorted() ?: emptyList()
    }

    LaunchedEffect(Unit) {
        val exlerRepository = ExlerRepository()
        val scheduleRepository = ScheduleRepository()
        val settings = ApplicationSettings.getCurrent()

        val scheduleTeachers = scheduleRepository.getScheduleFromCacheOrNull(settings.selectedSchedule ?: GroupName(""))
            ?.days
            ?.flatMap { it.lessons }
            ?.flatMap { it.lectors }
            ?.map { it.teacherName.name }
            ?.distinct() ?: emptyList<String>().also { Logger.w("Failed to fetch schedule teachers from cache") }

        val exlerTeachers = exlerRepository.getTeachersFromCacheOrNull()
            ?.map { it.name }
            ?.distinct() ?: emptyList<String>().also { Logger.w("Failed to fetch exler teachers from cache") }

        cachedTeacherPredictions =
            (scheduleTeachers + exlerTeachers).distinctBy { it.lowercase().split(" ").sorted().joinToString(" ") }
                .filter { it.isNotBlank() }
        Logger.d("Teacher predictions loaded: ${cachedTeacherPredictions?.size} teachers")
    }

    AlertDialog(
        confirmButton = {
            Button(onClick = { onTeacherPicked(pickedTeacher) }) { Text(stringResource(Res.string.add)) }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text(stringResource(Res.string.cancel)) }
        },
        onDismissRequest = onDismissRequest,
        text = {
            Column {
                OutlinedTextField(
                    value = pickedTeacher,
                    onValueChange = { pickedTeacher = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(Res.string.teacher_name) + "…") },
                    singleLine = true,
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            onTeacherPicked(pickedTeacher)
                            onDismissRequest()
                        }
                    ),
                )

                LazyColumn(Modifier.fillMaxWidth().heightIn(max = 200.dp)) {
                    items(predictions) {
                        Text(
                            it,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    pickedTeacher = it
                                    onTeacherPicked(it)
                                    onDismissRequest()
                                }
                                .padding(8.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Start
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun EventCreateDialogButtons(
    scheduleName: String,
    onDismissRequest: () -> Unit,
    onEventCreateRequest: () -> Unit,
) {
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .padding(bottom = 4.dp, top = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                "${stringResource(Res.string.would_be_added_to)} $scheduleName",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                ),
            )
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(Res.string.cancel))
            }
            Spacer(Modifier.width(8.dp))
            Button(onClick = onEventCreateRequest) {
                Text(stringResource(Res.string.done))
            }
        }
    }
}