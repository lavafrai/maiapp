package ru.lavafrai.maiapp.network.exler.parser

import com.fleeksoft.ksoup.Ksoup
import ru.lavafrai.maiapp.models.exler.ExlerFaculty
import ru.lavafrai.maiapp.models.exler.ExlerTeacher
import ru.lavafrai.maiapp.utils.mapAsync

suspend fun parseTeacherList(
    httpGet: HttpGet,
): List<ExlerTeacher> {
    val faculties = parseFaculties(httpGet)
    val teachers = faculties
        .mapAsync { faculty -> parseTeachers(httpGet, faculty) }
        .flatten()
        .distinctBy { it.path }
        .distinctBy { it.name }

    return teachers
}

suspend fun parseTeachers(
    httpGet: HttpGet,
    faculty: ExlerFaculty,
): List<ExlerTeacher> {
    val teacherNameMatcher = "\\S+\\s\\S+\\s\\S+".toRegex()
    val teachersHtml = httpGet("https://mai-exler.ru${faculty.path}")
    val teachersDocument = Ksoup.parse(teachersHtml)
    val teachers = teachersDocument
        .selectFirst("body > center > table > tbody > tr > td:nth-child(1) > table > tbody > tr > td > ol")!!
        .children()
        .map { it.selectFirst("a")!! }
        .map { ExlerTeacher(internalName = it.text(), path = it.attr("href"), faculty = faculty) }

    return teachers
}

suspend fun parseFaculties(
    httpGet: HttpGet,
): List<ExlerFaculty> {
    val facultiesHtml = httpGet("https://mai-exler.ru/prepods/")
    val facultiesDocument = Ksoup.parse(facultiesHtml)
    val faculties = facultiesDocument
        .selectFirst("body > center > table > tbody > tr > td:nth-child(1) > table > tbody > tr > td > div > table > tbody > tr > td > table > tbody")!!
        .children()
        .filterNot { it.text().contains("Преподов всего") }
        .filterNot { it.text().contains("Алфавитный список всех преподавателей") }
        .map { it.selectFirst("td:nth-child(3) > div > b > a")!! }
        .map { ExlerFaculty(name = it.text(), path = it.attr("href")) }
    return faculties
}