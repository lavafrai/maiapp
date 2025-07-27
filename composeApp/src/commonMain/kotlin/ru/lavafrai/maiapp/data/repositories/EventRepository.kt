package ru.lavafrai.maiapp.data.repositories

import co.touchlab.kermit.Logger
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.lavafrai.maiapp.models.events.Event
import ru.lavafrai.maiapp.models.events.SimpleEvent
import ru.lavafrai.maiapp.models.schedule.ScheduleId
import kotlin.uuid.Uuid

class EventRepository: BaseRepository() {
    val storageWriteMutex = Mutex()


    fun listAllEvents(scheduleId: ScheduleId): List<Event> {
        val events = try {
            storage.getStringOrNull(buildEventKey(scheduleId))?.let { json.decodeFromString<List<Event>>(it) } ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList<Event>()
        }

        return events
    }

    suspend fun createEvent(event: SimpleEvent, scheduleId: ScheduleId): SimpleEvent {
        val eventCreationUuid = Uuid.random()
        val eventToCreate = event.copy(_uuid = eventCreationUuid)
        storageWriteMutex.withLock {
            val currentEvents = listAllEvents(scheduleId)
            val updatedEvents = currentEvents + eventToCreate
            Logger.i("Creating event with UUID: $eventCreationUuid")

            storage.putString(buildEventKey(scheduleId), json.encodeToString(updatedEvents))
        }
        return eventToCreate
    }

    companion object {
        fun buildEventKey(scheduleId: ScheduleId): String {
            return "events:${scheduleId.scheduleId}"
        }
    }
}