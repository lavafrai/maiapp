package ru.lavafrai.maiapp.models.schedule

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.lavafrai.maiapp.models.events.Event
import ru.lavafrai.maiapp.models.time.DateRange
import ru.lavafrai.maiapp.models.time.week


@Serializable
data class Schedule(
    @SerialName("name") val name: String,
    @SerialName("id") val id: BaseScheduleId,
    @SerialName("created") val created: Long,
    @SerialName("cached") val cached: Long,
    @SerialName("days") val days: List<ScheduleDay>,
) {
    private val weeks: List<DateRange>
        get() {
            var firstWeek = days.minByOrNull { it.date }?.date?.week() ?: return emptyList()
            val lastWeek = days.maxByOrNull { it.date }?.date?.week() ?: return emptyList()
            // if (firstWeek == null || lastWeek == null) return emptyList()

            val weeks = mutableListOf<DateRange>()
            weeks.add(firstWeek)
            do {
                firstWeek = firstWeek.plusDays(7)
                weeks.add(firstWeek)
            } while (firstWeek.startDate < lastWeek.endDate)

            return weeks
        }

    fun weeks(events: List<Event>): List<DateRange> {
        val firstScheduleWeek = days.minByOrNull { it.date }?.date?.week()
        val lastScheduleWeek = days.maxByOrNull { it.date }?.date?.week()
        val renderedEvents = events.flatMap { it.renderForDateRange(null) }
        val firstEventWeek = renderedEvents.minByOrNull { it.date }?.date?.week()
        val lastEventWeek = renderedEvents.maxByOrNull { it.date }?.date?.week()
        if ((firstScheduleWeek == null && lastScheduleWeek == null) || (lastEventWeek == null && firstEventWeek == null)) return emptyList()

        var firstWeek = listOfNotNull(firstScheduleWeek, firstEventWeek).minByOrNull { it.startDate }!!
        val lastWeek = listOfNotNull(lastScheduleWeek, lastEventWeek).maxByOrNull { it.startDate }!!

        val weeks = mutableListOf<DateRange>()
        weeks.add(firstWeek)
        do {
            firstWeek = firstWeek.plusDays(7)
            weeks.add(firstWeek)
        } while (firstWeek.startDate < lastWeek.endDate)

        return weeks
    }

    /*
    private var weeks: MutableList<ScheduleWeekId>? = null

    fun getWeeks(): List<ScheduleWeekId> {
        var i = 1

        if (weeks == null) {
            weeks = mutableListOf()
            days.forEach {
                val week = it.date!!.getWeek()
                if (weeks!!.find {week == it.range} == null) {weeks!!.add(ScheduleWeekId(i, it.date.getWeek()));i++}

            }
            weeks!!.sortBy { scheduleWeekId -> scheduleWeekId.range.startDate }
        }

        return weeks!!
    }

    fun getWeek(number: Int): List<ScheduleDay> {
        val weekId = getWeeks().find { it.number == number } ?: return listOf()
        return days.filter { weekId.range.contains(it.date!!) }.sortedBy { it.date }
    }

    fun getCurrentWeekSchedule(): List<ScheduleDay> {
        val week = Date.now().getWeek()
        return days.filter { week.contains(it.date!!) }.sortedBy { it.date }
    }

    fun getScheduleOfDay(day: Date): ScheduleDay {
        return days.find { it.date == day } ?: ScheduleDay(day, day.toLocalDate().dayOfWeek.castToSerializable(), listOf())
    }
    */
}

/*
fun getEmptySchedule(): Schedule {
return Schedule(Group(""), 0, listOf())
}
*/