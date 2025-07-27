package ru.lavafrai.maiapp.utils

import kotlinx.datetime.LocalTime

object PairTimeHelper {
    private val pairTimes = listOf(
        LocalTime(9, 0) to LocalTime(10, 30),
        LocalTime(10, 45) to LocalTime(12, 15),
        LocalTime(13, 0) to LocalTime(14, 30),
        LocalTime(14, 45) to LocalTime(16, 15),
        LocalTime(16, 30) to LocalTime(18, 0),
        LocalTime(18, 15) to LocalTime(19, 45),
        LocalTime(20, 0) to LocalTime(21, 30),
    )

    val pairCount: Int
        get() = pairTimes.size

    fun getPairTime(pairNumber: Int): Pair<LocalTime, LocalTime> {
        require(pairNumber in 1..pairTimes.size) { "Pair number must be between 1 and ${pairTimes.size}" }
        return pairTimes[pairNumber - 1]
    }

    fun getPairsInRange(startTime: LocalTime, endTime: LocalTime): List<Int> {
        if (startTime !in pairTimes.map { it.first }) return emptyList()
        if (endTime !in pairTimes.map { it.second }) return emptyList()
        if (startTime > endTime) return emptyList()

        return pairTimes.mapIndexedNotNull { index, pair ->
            if (pair.first >= startTime && pair.second <= endTime) index + 1 else null
        }
    }
}