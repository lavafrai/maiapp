package ru.lavafrai.maiapp.fragments.events

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.lavafrai.maiapp.models.events.Event
import ru.lavafrai.maiapp.models.events.SimpleEvent


@Composable
fun EventEditorCard(
    event: Event,
    expanded: Boolean,
    onExpandedChange: (isExpanded: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (event) {
        is SimpleEvent -> SimpleEventEditorCard(
            event = event,
            expanded = expanded,
            onExpandedChange = onExpandedChange,
            modifier = modifier
        )
        else -> Text("Unsupported event type: ${event::class.simpleName}")
    }
}
