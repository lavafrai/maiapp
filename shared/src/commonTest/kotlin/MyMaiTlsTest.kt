package ru.lavafrai.maiapp

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class MyMaiTlsTest {
    @Test fun customRootIsLimitedToAccountHosts() {
        for (host in listOf("esia.mai.ru", "my.mai.ru", "ESIA.MAI.RU", "my.mai.ru.")) {
            assertTrue(isMyMaiHost(host), host)
        }
        for (host in listOf("mai.ru", "other.mai.ru", "evilmy.mai.ru", "my.mai.ru.evil.com",
            "sub.esia.mai.ru", "my.mai.ru..", "127.0.0.1", "", " my.mai.ru")) {
            assertFalse(isMyMaiHost(host), host)
        }
    }

    @Test fun plainHttpIsRejectedBeforeSending() = runTest {
        val client = HttpClient(MockEngine { error("HTTP must not reach the engine") }) {
            configureMyMaiClient()
        }
        try {
            assertFailsWith<IllegalArgumentException> { client.get("http://esia.mai.ru/") }
        } finally { client.close() }
    }

    @Test fun httpsRedirectCannotDowngradeToHttp() = runTest {
        var requests = 0
        val client = HttpClient(MockEngine {
            requests++
            respond("", HttpStatusCode.Found, headersOf(HttpHeaders.Location, "http://my.mai.ru/"))
        }) { configureMyMaiClient() }
        try {
            assertEquals(HttpStatusCode.Found, client.get("https://esia.mai.ru/").status)
            assertEquals(1, requests)
        } finally { client.close() }
    }
}
