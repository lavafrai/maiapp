package ru.lavafrai.maiapp.network.mai

import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.Sign
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.lavafrai.maiapp.HttpClientProvider
import ru.lavafrai.maiapp.exceptions.NotFoundException
import ru.lavafrai.maiapp.models.group.Group
import ru.lavafrai.maiapp.models.schedule.Schedule
import ru.lavafrai.maiapp.models.schedule.TeacherId
import ru.lavafrai.maiapp.network.mai.raw.parseRawSchedule
import org.kotlincrypto.hash.md.MD5

class MaiRepository(
    private val httpClient: HttpClient = HttpClientProvider.default,
) {
    private val teachersAccess = Mutex()
    private val teachers = mutableListOf<TeacherId>()

    suspend fun getGroups() = httpClient
        .get("https://public.mai.ru/schedule/data/groups.json")
        .body<List<Group>>()
        .filter { it.name != "Для внеучебных мероприятий (служебная)" }

    suspend fun getGroupSchedule(group: String) = httpClient
        .get("https://public.mai.ru/schedule/data/${md5(group.uppercase())}.json")
        .apply { if (this.status == HttpStatusCode.NotFound) throw NotFoundException("Failed to get schedule($group): Not found") }
        .bodyAsText()
        .parseRawSchedule()
        .also {
            teachersAccess.withLock {
                it.bypassTeachers { teacher ->
                    if (teacher.uid == "00000000-0000-0000-0000-000000000000") return@bypassTeachers
                    if (!teachers.any { teacher.uid == it.uid }) teachers.add(teacher)
                }
            }
        }
}


fun md5(input: String): String {
    val md = MD5()
    return BigInteger
        .fromByteArray(md.digest(input.toByteArray()), sign = Sign.POSITIVE)
        .toString(16).padStart(32, '0')
}

fun Schedule.bypassTeachers(block: (TeacherId) -> Unit) = days.forEach { day ->
    day.lessons.forEach { lesson ->
        lesson.lectors.forEach { teacher ->
            block(teacher)
        }
    }
}
