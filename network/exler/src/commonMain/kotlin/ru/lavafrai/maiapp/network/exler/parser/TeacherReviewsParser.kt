package ru.lavafrai.maiapp.network.exler.parser

import com.fleeksoft.ksoup.Ksoup
import ru.lavafrai.maiapp.models.exler.ExlerTeacher
import ru.lavafrai.maiapp.models.exler.ExlerTeacherInfo
import ru.lavafrai.maiapp.models.exler.ExlerTeacherReview

suspend fun parseTeacherReviews(
    httpGet: HttpGet,
    teacherId: ExlerTeacher,
): ExlerTeacherInfo {
    val reviewsPage = httpGet("https://mai-exler.ru${teacherId.path}")
    val reviewsElement = Ksoup.parse(reviewsPage).select("body > center > table > tbody > tr > td:nth-child(1) > table > tbody > tr > td")
    val reviewTexts = "<!--subscribeBegin-->([\\s\\S]+?)<!--subscribeEnd-->"
        .toRegex()
        .findAll(reviewsElement.toString())
        .map { it.groupValues[1] }.toList()
    val reviews = reviewTexts.subList(1, reviewTexts.size).map { parseTeacherReview(httpGet, it, teacherId) }

    val baseInfo = parseBaseTeacherInfo(
        httpGet,
        defaultName = teacherId.name,
        text = reviewTexts[0]
    )
    val info = baseInfo.copy(
        reviews = reviews,
    )

    return info
}

suspend fun parseTeacherReview(
    httpGet: HttpGet,
    text: String,
    teacherId: ExlerTeacher,
): ExlerTeacherReview {
    val reviewMeta = "<small>(.+?)</small>".toRegex().find(text)?.groupValues?.get(1)?.trim()!!
    val reviewText = text.replace("<small>(.+?)</small>".toRegex(), "").trim()

    return ExlerTeacherReview(
        author = "reviewMeta",
        source = "reviewMeta",
        publishTime = "reviewMeta",
        hypertext = reviewText,
    )
}

suspend fun parseBaseTeacherInfo(
    httpGet: HttpGet,
    defaultName: String,
    text: String,
): ExlerTeacherInfo {
    val baseInfoElementText = Ksoup.parse(text).wholeText()
    val name = "<font color=\"#006699\"><b>(.+?)</b></font>".toRegex().findAll(text).lastOrNull()?.groupValues?.get(1)?.trim() ?: defaultName
    val faculty = "Факультет:(.+?)\n".toRegex().findAll(baseInfoElementText).lastOrNull()?.groupValues?.get(1)?.trim()
    val department = "Кафедра:(.+?)\n".toRegex().findAll(baseInfoElementText).lastOrNull()?.groupValues?.get(1)?.trim()
    return ExlerTeacherInfo(
        name = name,
        faculty = faculty,
        department = department,
        photo = null,
        reviews = emptyList(),
    )
}