package ru.lavafrai.maiapp.viewmodels.eventsEditor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.launch
import ru.lavafrai.maiapp.data.Loadable
import ru.lavafrai.maiapp.data.repositories.EventRepository
import ru.lavafrai.maiapp.models.schedule.ScheduleId
import ru.lavafrai.maiapp.viewmodels.MaiAppViewModel
import kotlin.reflect.KClass

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
            emit(initialState)

            launchCatching(onError = {
                it.printStackTrace()
                emit(state.value.copy(
                    events = Loadable.error(it),
                ))
            }) {
                val events = eventsRepository.listAllEvents(scheduleId)
                emit(
                    state.value.copy(
                        events = Loadable.actual(events),
                    )
                )
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