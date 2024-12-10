package mai.raw

import kotlinx.datetime.Clock
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import ru.lavafrai.maiapp.JsonProvider
import ru.lavafrai.maiapp.models.schedule.Schedule

fun String.parseRawSchedule(): Schedule {
    val json = JsonProvider.tolerantJson
    val jsonObject = json.parseToJsonElement(this).jsonObject

    return Schedule(
        jsonObject["group"]?.jsonPrimitive?.content!!,
        Clock.System.now().epochSeconds,
        0,
        jsonObject.filter { it.key != "group" }.map { json.decodeFromJsonElement<ScheduleDayRaw>(it.value).toScheduleDay(json, it.key) }
    )
}