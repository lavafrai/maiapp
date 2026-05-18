package ru.lavafrai.maiapp.data.repositories

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.lavafrai.maiapp.models.events.Event
import ru.lavafrai.maiapp.models.events.SimpleEvent
import ru.lavafrai.maiapp.models.schedule.BaseScheduleId
import ru.lavafrai.maiapp.models.schedule.ScheduleId
import kotlin.uuid.Uuid

object EventRepository: BaseRepository() {
    val storageWriteMutex = Mutex()
    private val flows: MutableMap<ScheduleId, MutableStateFlow<List<Event>>> = mutableMapOf()

    fun follow(group: ScheduleId): StateFlow<List<Event>> {
        return flows.getOrPut(group) {
            MutableStateFlow(listAllEvents(group))
        }
    }


    fun listAllEvents(scheduleId: ScheduleId): List<Event> {
        val events = try {
            storage.getStringOrNull(buildEventKey(scheduleId))?.let { json.decodeFromString<List<Event>>(it) } ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList<Event>()
        }

        return events
    }

    fun listAllSchedules(): List<ScheduleId> {
        return storage.keys
            .filter { it.startsWith("events:") }
            .mapNotNull {
                val scheduleIdStr = it.removePrefix("events:")
                if (scheduleIdStr.isBlank()) null
                else BaseScheduleId(scheduleIdStr)
            }
    }

    suspend fun createEvent(event: SimpleEvent, scheduleId: ScheduleId): SimpleEvent {
        val eventCreationUuid = Uuid.random()
        val eventToCreate = event.copy(_uuid = eventCreationUuid)
        storageWriteMutex.withLock {
            val currentEvents = listAllEvents(scheduleId)
            val updatedEvents = currentEvents + eventToCreate
            Logger.i("Creating event with UUID: $eventCreationUuid")

            storage.putString(buildEventKey(scheduleId), json.encodeToString(updatedEvents))
            flows[scheduleId]?.value = updatedEvents
        }
        return eventToCreate
    }

    suspend fun updateEvent(eventId: Uuid, event: SimpleEvent) {
        storageWriteMutex.withLock {
            val allSchedules = listAllSchedules()
            for (scheduleId in allSchedules) {
                val currentEvents = listAllEvents(scheduleId)
                val updatedEvents = currentEvents.map {
                    // WARN: need to be reworked after adding new Event types
                    if (it.uid == eventId) event.copy(_uuid = (it as SimpleEvent)._uuid) else it
                }
                if (updatedEvents.any { it.uid == eventId }) {
                    Logger.i("Updating event with UUID: $eventId")
                    storage.putString(buildEventKey(scheduleId), json.encodeToString(updatedEvents))
                    flows[scheduleId]?.value = updatedEvents
                    return
                }
            }
        }
    }

    suspend fun deleteEvent(eventId: Uuid) {
        storageWriteMutex.withLock {
            val allSchedules = listAllSchedules()
            for (scheduleId in allSchedules) {
                val currentEvents = listAllEvents(scheduleId)
                val updatedEvents = currentEvents.filter { it.uid != eventId }
                if (updatedEvents.size != currentEvents.size) {
                    Logger.i("Deleting event with UUID: $eventId")
                    storage.putString(buildEventKey(scheduleId), json.encodeToString(updatedEvents))
                    return
                }
                flows[scheduleId]?.value = updatedEvents
            }
        }
    }

    fun getScheduleIdForEvent(event: Event): ScheduleId {
        val allSchedules = listAllSchedules()
        for (scheduleId in allSchedules) {
            val currentEvents = listAllEvents(scheduleId)
            if (currentEvents.any { it.uid == event.uid }) return scheduleId
        }
        return BaseScheduleId("unknown")
    }

    fun buildEventKey(scheduleId: ScheduleId): String {
        return "events:${scheduleId.scheduleId}"
    }
}