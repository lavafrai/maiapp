package ru.lavafrai.maiapp.viewmodels.eventsEditor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.launch
import ru.lavafrai.maiapp.data.Loadable
import ru.lavafrai.maiapp.data.repositories.EventRepository
import ru.lavafrai.maiapp.models.events.SimpleEvent
import ru.lavafrai.maiapp.models.schedule.ScheduleId
import ru.lavafrai.maiapp.viewmodels.MaiAppViewModel
import kotlin.reflect.KClass
import kotlin.uuid.Uuid

class EventsEditorViewModel(
    val scheduleId: ScheduleId,
): MaiAppViewModel<EventsEditorState>(
    EventsEditorState(
        scheduleId = scheduleId,
        events = Loadable.loading(),
    )
) {
    val eventsRepository = EventRepository

    init {
        startLoading()
    }

    fun startLoading() {
        viewModelScope.launch (dispatchers.IO) {

            launchCatching(onError = {
                it.printStackTrace()
                emit(state.value.copy(
                    events = Loadable.error(it),
                ))
            }) {
                emitAsync(initialState)
                val events = eventsRepository.listAllEvents(scheduleId)
                emitAsync(
                    state.value.copy(
                        events = Loadable.actual(events),
                    )
                )
            }
        }
    }

    fun createEvent(event: SimpleEvent) {
        viewModelScope.launch (dispatchers.IO) {
            launchCatching(onError = {
                it.printStackTrace()
            }) {
                eventsRepository.createEvent(event, stateValue.scheduleId)
                startLoading()
            }
        }
    }

    fun deleteEvent(eventId: Uuid) {
        viewModelScope.launch (dispatchers.IO) {
            launchCatching(onError = {
                it.printStackTrace()
            }) {
                eventsRepository.deleteEvent(eventId)
                startLoading()
            }
        }
    }

    fun updateEvent(eventId: Uuid, event: SimpleEvent) {
        viewModelScope.launch (dispatchers.IO) {
            launchCatching(onError = {
                it.printStackTrace()
            }) {
                eventsRepository.updateEvent(eventId, event)
                startLoading()
            }
        }
    }

    class Factory(
        private val scheduleId: ScheduleId,
    ): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
            return EventsEditorViewModel(
                scheduleId = scheduleId,
            ) as T
        }
    }
}