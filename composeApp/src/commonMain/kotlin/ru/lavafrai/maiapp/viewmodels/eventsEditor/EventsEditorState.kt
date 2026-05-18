package ru.lavafrai.maiapp.viewmodels.eventsEditor

import ru.lavafrai.maiapp.data.Loadable
import ru.lavafrai.maiapp.models.events.Event
import ru.lavafrai.maiapp.models.schedule.ScheduleId

data class EventsEditorState(
    val scheduleId: ScheduleId,
    val events: Loadable<List<Event>>,
)