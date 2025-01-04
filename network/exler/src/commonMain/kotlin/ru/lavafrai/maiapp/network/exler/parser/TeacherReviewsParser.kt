package ru.lavafrai.maiapp.network.exler.parser

import com.fleeksoft.ksoup.Ksoup
import ru.lavafrai.maiapp.models.exler.ExlerTeacher
import ru.lavafrai.maiapp.models.exler.ExlerTeacherInfo
import ru.lavafrai.maiapp.models.exler.ExlerTeacherReview
import ru.lavafrai.maiapp.utils.contextual

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
        // .also { if (it.isBlank()) return null }
        .also { if (it.contains("<table")) {println("skip table: $teacherId") ; return null} } // cause: https://mai-exler.ru/prepods/08/vestyak/
        .also { if (it.contains("<b>ФРАЗЫ</b>")) {println("skip phrases: $teacherId") ; return null} } // cause: https://mai-exler.ru/prepods/08/sharikov/
        .contextual(case = { trim().startsWith("small&gt;") }) { println("fix: $teacherId") ; replace("small&gt;", "<small>") } // cause: https://mai-exler.ru/prepods/02/uskova/

    val reviewMeta = "<small>([\\s\\S]+?)</font>".toRegex().find(normalizedText)?.groupValues?.get(1)?.trim() ?: run {
        throw RuntimeException("Failed to parse review meta")
    }
    val reviewMetaClear = Ksoup.parse(reviewMeta).wholeText()
    val reviewText = normalizedText
        .replace(reviewMeta, "")
        .replace("&nbsp;", " ")
        .trim()

    val reviewAuthor = "<b>Автор(ы)?:</b> (.+?)<br>".toRegex().find(reviewMeta)?.groupValues?.get(2)?.trim()

    return ExlerTeacherReview(
        author = reviewAuthor,
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