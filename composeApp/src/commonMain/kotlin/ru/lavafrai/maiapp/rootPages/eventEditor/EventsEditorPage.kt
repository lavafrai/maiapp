package ru.lavafrai.maiapp.rootPages.eventEditor

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import compose.icons.FeatherIcons
import compose.icons.feathericons.ArrowLeft
import maiapp.composeapp.generated.resources.Res
import maiapp.composeapp.generated.resources.events
import org.jetbrains.compose.resources.stringResource
import ru.lavafrai.maiapp.data.repositories.readableName
import ru.lavafrai.maiapp.fragments.LoadableView
import ru.lavafrai.maiapp.fragments.events.EventCreateDialog
import ru.lavafrai.maiapp.models.schedule.ScheduleId
import ru.lavafrai.maiapp.rootPages.main.MainPageTitle
import ru.lavafrai.maiapp.viewmodels.eventsEditor.EventsEditorViewModel

@Composable
fun EventsEditorPage(
    onNavigateBack: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    scheduleId: ScheduleId,
) {
    val viewModel: EventsEditorViewModel = viewModel(factory = EventsEditorViewModel.Factory(scheduleId))
    val viewState by viewModel.state.collectAsState()
    var eventCreteDialogOpened by remember { mutableStateOf(false) }

    Column {
        MainPageTitle(
            titleText = { Text(stringResource(Res.string.events)) },
            subtitleText = { Text(viewState.scheduleId.readableName()) },
            leftButton = { IconButton(onNavigateBack) { Icon(FeatherIcons.ArrowLeft, "Back icon") } },
        )

        LoadableView(
            state = viewState.events,
            retry = viewModel::startLoading,
            modifier = Modifier.fillMaxSize(),
        ) {
            EventsEditorView(
                events = it,
                scheduleId = scheduleId,
                sharedTransitionScope = sharedTransitionScope,
                animatedContentScope = animatedContentScope,
                onAddEventRequest = { eventCreteDialogOpened = true },
                onEventRemove = { viewModel.deleteEvent(it) },
                onEventUpdate = { id, new -> viewModel.updateEvent(id, new) },
            )
        }
    }

    if (eventCreteDialogOpened) EventCreateDialog(
        onDismissRequest = { eventCreteDialogOpened = false },
        onEventCreated = { viewModel.createEvent(it); eventCreteDialogOpened = false },
        scheduleName = viewState.scheduleId.scheduleId,
    )
}