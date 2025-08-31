package ru.lavafrai.maiapp.models.events

import kotlinx.datetime.DatePeriod

enum class SimpleEventPeriod {
    Single,
    Weekly,
    Biweekly,
    Monthly;

    val datePeriod: DatePeriod
        get() = when (this) {
            Single -> DatePeriod(Int.MAX_VALUE)
            Weekly -> DatePeriod(days = 7)
            Biweekly -> DatePeriod(days = 14)
            Monthly -> DatePeriod(months = 1)
        }
}