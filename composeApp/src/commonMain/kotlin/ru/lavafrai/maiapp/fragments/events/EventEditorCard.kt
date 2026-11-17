package ru.lavafrai.maiapp.fragments.events

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import maiapp.composeapp.generated.resources.Res
import maiapp.composeapp.generated.resources.unsupported_event_type
import org.jetbrains.compose.resources.stringResource
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
        else -> Text(stringResource(Res.string.unsupported_event_type, event::class.simpleName ?: "?"))
    }
}
