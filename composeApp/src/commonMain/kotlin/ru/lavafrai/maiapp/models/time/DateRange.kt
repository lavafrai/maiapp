package ru.lavafrai.maiapp.models.time

import kotlinx.datetime.LocalDate
import kotlinx.datetime.serializers.LocalDateComponentSerializer
import kotlinx.serialization.Serializable


@Serializable
data class DateRange(
    @Serializable(LocalDateComponentSerializer::class) val startDate: LocalDate,
    @Serializable(LocalDateComponentSerializer::class) val endDate: LocalDate,
) {
    operator fun contains(another: LocalDate): Boolean {
        return another in startDate..endDate
    }

/*
    override fun toString(): String {
        return "$startDate - $endDate"
    }


    fun isNow(): Boolean {
        return Date.now() in this
    }

    fun plus(field: Int, amount: Int): DateRange {
        return DateRange(
                startDate.plus(field, amount),
                endDate.plus(field, amount)
        )
    }

    fun minus(field: Int, amount: Int): DateRange {
        return plus(field, -amount)
    }

    fun plusDays(amount: Int): DateRange {
        return plus(Calendar.DATE, amount)
    }

    fun minusDays(amount: Int): DateRange {
        return minus(Calendar.DATE, amount)
    }


    companion object {
        fun parse(string: String): DateRange {
            return DateRange(
                Date.parse(string.split(" - ")[0]),
                Date.parse(string.split(" - ")[1]),
            )
        }
    }
    */
}
