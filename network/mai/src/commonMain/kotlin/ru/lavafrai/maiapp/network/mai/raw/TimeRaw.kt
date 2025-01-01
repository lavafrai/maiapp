package ru.lavafrai.maiapp.network.mai.raw

import ru.lavafrai.maiapp.models.time.Time

typealias TimeRaw = String

fun TimeRaw.toTime() = Time(this)
