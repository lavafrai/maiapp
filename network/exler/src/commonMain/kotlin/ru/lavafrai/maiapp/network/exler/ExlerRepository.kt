package ru.lavafrai.maiapp.network.exler

import com.github.michaelbull.retry.policy.constantDelay
import com.github.michaelbull.retry.policy.plus
import com.github.michaelbull.retry.policy.stopAtRetries
import com.github.michaelbull.retry.retry
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import ru.lavafrai.maiapp.HttpClientProvider
import ru.lavafrai.maiapp.models.exler.ExlerTeacher
import ru.lavafrai.maiapp.models.exler.ExlerTeacherInfo
import ru.lavafrai.maiapp.models.time.now
import ru.lavafrai.maiapp.network.exler.parser.parseTeacherList
import ru.lavafrai.maiapp.network.exler.parser.parseTeacherReviews
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class ExlerRepository(
    private val httpClient: HttpClient = HttpClientProvider.default,
) {
    private val bannedTeacherReviews = listOf(
        "/prepods/03/chekanov/",
        "/prepods/08/kunitsyn/",
        "/prepods/war/pochuev/",
    )

    private var cachedTeachers: List<ExlerTeacher>? = null
    private var cachedTeachersTimestamp: Duration = 0.seconds
    private val cachedTeachersExpirationTime = 60.minutes

    suspend fun httpGet(url: String) = retry(constantDelay<Throwable>(1000) + stopAtRetries(3)) {
        val response = httpClient.get(url)
        if (!response.status.isSuccess()) {
            throw Exception("Failed to fetch $url: ${response.status}")
        }
        response.bodyAsText()
    }

    suspend fun getCachedExlerTeachers(): List<ExlerTeacher> {
        if (
            cachedTeachers == null ||
            LocalDateTime.now().toInstant(TimeZone.UTC).epochSeconds.seconds - cachedTeachersTimestamp > cachedTeachersExpirationTime
        ) {
            return getExlerTeachers()
        }
        return cachedTeachers!!
    }

    suspend fun getExlerTeacherByName(name: String): ExlerTeacher? {
        val teachers = getCachedExlerTeachers()
        return teachers.find { it.name.lowercase() == name.lowercase() }
    }

    suspend fun getExlerTeachers(): List<ExlerTeacher> {
        val teachers = parseTeacherList(this::httpGet)
        cachedTeachers = teachers
        cachedTeachersTimestamp = LocalDateTime.now().toInstant(TimeZone.UTC).epochSeconds.seconds
        return teachers.filter {
            it.path !in bannedTeacherReviews
        }
    }

    suspend fun getExlerTeacherReviews(teacherId: ExlerTeacher): ExlerTeacherInfo {
        return parseTeacherReviews(this::httpGet, teacherId)
    }
}
