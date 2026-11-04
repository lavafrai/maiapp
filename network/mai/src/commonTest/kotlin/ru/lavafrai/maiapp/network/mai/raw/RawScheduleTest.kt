package ru.lavafrai.maiapp.network.mai.raw

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class RawScheduleTest {
    // Так МАИ отдаёт занятия по подгруппам: одна пара, а внутри — несколько предметов
    private val scheduleWithTwoSubjectsInOnePair = """
        {
          "group": "М8О-101БВ-26",
          "schedule": {
            "01.09.2026": {
              "day": "Вт",
              "pairs": {
                "09:00:00": {
                  "Математика": {
                    "time_start": "9:00:00",
                    "time_end": "10:30:00",
                    "lector": {"teacher-1": "иванов иван иванович"},
                    "type": {"ЛК": 1},
                    "room": {"room-1": "ГУК 1"},
                    "lms": "",
                    "teams": "",
                    "other": ""
                  },
                  "Физика": {
                    "time_start": "9:00:00",
                    "time_end": "10:30:00",
                    "lector": {"teacher-2": "петров пётр петрович"},
                    "type": {"ЛК": 1},
                    "room": {"room-2": "ГУК 2"},
                    "lms": "",
                    "teams": "",
                    "other": ""
                  }
                }
              }
            }
          }
        }
    """.trimIndent()

    @Test
    fun keepsEverySubjectOfOnePair() {
        val schedule = scheduleWithTwoSubjectsInOnePair.parseRawSchedule()

        assertEquals(1, schedule.days.size)
        assertEquals(listOf("Математика", "Физика"), schedule.days.single().lessons.map { it.name })
    }

    @Test
    fun keepsDetailsOfEverySubject() {
        val lessons = scheduleWithTwoSubjectsInOnePair.parseRawSchedule().days.single().lessons

        assertEquals(LocalDate(2026, 9, 1), lessons[0].day)
        assertEquals(listOf("Иванов Иван Иванович"), lessons[0].lectors.map { it.name })
        assertEquals(listOf("ГУК 1"), lessons[0].rooms.map { it.name })

        assertEquals(LocalDate(2026, 9, 1), lessons[1].day)
        assertEquals(listOf("Петров Пётр Петрович"), lessons[1].lectors.map { it.name })
        assertEquals(listOf("ГУК 2"), lessons[1].rooms.map { it.name })
    }

    @Test
    fun subjectsOfOnePairHaveDifferentUids() {
        val lessons = scheduleWithTwoSubjectsInOnePair.parseRawSchedule().days.single().lessons

        assertEquals(2, lessons.map { it.getUid() }.distinct().size)
    }
}
