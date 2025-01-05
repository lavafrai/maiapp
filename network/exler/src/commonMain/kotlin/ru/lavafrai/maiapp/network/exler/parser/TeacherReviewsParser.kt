package ru.lavafrai.maiapp.network.exler.parser

import com.fleeksoft.ksoup.Ksoup
import ru.lavafrai.maiapp.models.exler.ExlerTeacher
import ru.lavafrai.maiapp.models.exler.ExlerTeacherInfo
import ru.lavafrai.maiapp.models.exler.ExlerTeacherReview
import ru.lavafrai.maiapp.utils.contextual
import ru.lavafrai.maiapp.utils.modify

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
    val reviews = reviewTexts
        .subList(1, reviewTexts.size)
        .map { parseTeacherReview(httpGet, it, teacherId) }
        .filterNotNull()

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
): ExlerTeacherReview? {
    val normalizedText = text
        .also { if (it.contains("<table")) {println("skip table: $teacherId") ; return null} } // cause: https://mai-exler.ru/prepods/08/vestyak/
        .also { if (it.contains("<b>ФРАЗЫ</b>")) {println("skip phrases: $teacherId") ; return null} } // cause: https://mai-exler.ru/prepods/08/sharikov/
        .contextual(case = { trim().startsWith("small&gt;") }) { println("fix: $teacherId") ; replace("small&gt;", "<small>") } // cause: https://mai-exler.ru/prepods/02/uskova/
        .modify() { replace("&nbsp;", " ") }
        .modify() { replace("<small><font color=\"#666666\"> </font></small>", "") } // cause: https://mai-exler.ru/prepods/02/mikhailova/

    val reviewMeta = Ksoup.parse(normalizedText).select("small").joinToString("\n")
    if (reviewMeta.isBlank()) {
        return null
    }

    val reviewText = normalizedText
        .replace(reviewMeta, "")
        .trim()

    val reviewAuthor = "<b>Автор(ы)?:\\s?+</b>([\\s\\S]+?)(<br>|</font>)".toRegex().find(reviewMeta)?.groupValues?.get(2)?.trim()
        ?: "<b>Записал?:\\s?+</b>([\\s\\S]+?)(<br>|</font>)".toRegex().find(reviewMeta)?.groupValues?.get(2)?.trim()
        ?: "<b>Прислал?:\\s?+</b>([\\s\\S]+?)(<br>|</font>)".toRegex().find(reviewMeta)?.groupValues?.get(2)?.trim()
    val reviewSource = "<b>Источник(и)?:\\s?+</b>([\\s\\S]+?)(<br>|</font>)".toRegex().find(reviewMeta)?.groupValues?.get(2)?.trim()
    val reviewPublishTime = "<b>Опубликовано:\\s?+</b>([\\s\\S]+?)(<br>|</font>)".toRegex().find(reviewMeta)?.groupValues?.get(1)?.trim()

    return ExlerTeacherReview(
        author = reviewAuthor,
        source = reviewSource,
        publishTime = reviewPublishTime,
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