package ru.lavafrai.maiapp.models.exler

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ExlerTeacherTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun readsTeacherWithoutMaiUuid() {
        val teacher = json.decodeFromString<ExlerTeacher>(
            """
            {
              "name": "Курочкина Ирина Юрьевна",
              "path": "/prepods/pum/kurochkina_iy/",
              "faculty": { "name": "Предуниверсарий МАИ", "path": "/prepods/pum/" },
              "nameHash": -104749058
            }
            """.trimIndent()
        )

        assertNull(teacher.maiUuid)
    }

    @Test
    fun readsTeacherWithMaiUuid() {
        val teacher = json.decodeFromString<ExlerTeacher>(
            """
            {
              "name": "Курочкина Ирина Юрьевна",
              "path": "/prepods/pum/kurochkina_iy/",
              "faculty": { "name": "Предуниверсарий МАИ", "path": "/prepods/pum/" },
              "nameHash": -104749058,
              "maiUuid": "0b3c5d7e-1234-4abc-9def-0123456789ab"
            }
            """.trimIndent()
        )

        assertEquals("0b3c5d7e-1234-4abc-9def-0123456789ab", teacher.maiUuid)
    }
}
