package ru.lavafrai.maiapp.rootPages.eventEditor

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import compose.icons.FeatherIcons
import compose.icons.feathericons.Plus
import ru.lavafrai.maiapp.fragments.events.SimpleEventEditorCard
import ru.lavafrai.maiapp.models.events.SimpleEvent
import kotlin.uuid.Uuid

@Composable
fun EventsEditorView(
    events: List<ru.lavafrai.maiapp.models.events.Event>,
    scheduleId: ru.lavafrai.maiapp.models.schedule.ScheduleId,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onAddEventRequest: () -> Unit,
    onEventRemove: (Uuid) -> Unit,
    onEventUpdate: (Uuid, SimpleEvent) -> Unit,
) = Scaffold(
    floatingActionButton = {
        FloatingActionButton(
            onClick = onAddEventRequest
        ) {
            Icon(FeatherIcons.Plus, "Add event" )
        }
    }
) {
    LazyColumn(
        contentPadding = PaddingValues(top = 8.dp, bottom = it.calculateBottomPadding() + 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        items(
            events.size,
            key = { index -> events[index].uid }
        ) { index ->
            val event = events[index]
            var expanded by remember { androidx.compose.runtime.mutableStateOf(false) }

            if (event is SimpleEvent) SimpleEventEditorCard(
                event = event,
                expanded = expanded,
                onExpandedChange = { expanded = it },
                modifier = Modifier.animateItem(),
                onRemove = { onEventRemove(event.uid) },
                onUpdate = { updatedEvent -> onEventUpdate(event.uid, updatedEvent) },
            )
        }
    }
}