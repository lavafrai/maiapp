package ru.lavafrai.maiapp.network.mai.raw

import kotlinx.datetime.Clock
import kotlinx.serialization.json.*
import ru.lavafrai.maiapp.JsonProvider
import ru.lavafrai.maiapp.models.schedule.AbstractScheduleId
import ru.lavafrai.maiapp.models.schedule.Schedule
import ru.lavafrai.maiapp.models.schedule.ScheduleDay
import ru.lavafrai.maiapp.utils.contextual

fun String.parseRawSchedule(): Schedule {
    val json = JsonProvider.tolerantJson
    val jsonObject = json.parseToJsonElement(this).jsonObject

    val name = jsonObject["group"]?.jsonPrimitive?.content
        ?: jsonObject["name"]?.jsonPrimitive?.content
        ?: throw IllegalArgumentException("Invalid schedule json: $this")
    val scheduleObject = jsonObject
        .filter { it.key != "group" }
        .contextual(case = { contains("schedule") }) { this["schedule"]!!.jsonObject }
    val teacherMode = jsonObject["groups"] != null

    return Schedule(
        name = name,
        id = AbstractScheduleId(name),
        Clock.System.now().epochSeconds,
        0,
        if (teacherMode) scheduleObject.parseRawTeacherSchedule(json) else scheduleObject.parseRawSchedule(json),
    )
}

fun Map<String, JsonElement>.parseRawSchedule(json: Json): List<ScheduleDay> = map { json.decodeFromJsonElement<ScheduleDayRaw>(it.value).toScheduleDay(json, it.key, false) }
fun Map<String, JsonElement>.parseRawTeacherSchedule(json: Json): List<ScheduleDay> = map { json.decodeFromJsonElement<ScheduleDayRaw>(it.value).toScheduleDay(json, it.key, true) }