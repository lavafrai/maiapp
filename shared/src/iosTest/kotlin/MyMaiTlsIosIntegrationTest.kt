package ru.lavafrai.maiapp

import io.ktor.client.engine.darwin.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import platform.Foundation.NSProcessInfo
import kotlin.test.*

/** Run inside an app bundle with production Info.plist; see scripts/check-ios-tls.sh. */
class MyMaiTlsIosIntegrationTest {
    @Test fun accountHostsAndSystemRootsWork() = runBlocking {
        if (!liveChecksEnabled()) return@runBlocking
        val client = createMyMaiHttpClient()
        try {
            for (url in listOf("https://esia.mai.ru/auth/realms/lk_mai/", "https://my.mai.ru/", "https://example.com/")) {
                assertTrue(client.get(url).status.value in 200..499, url)
            }
        } finally { client.close() }
    }

    @Test fun invalidCertificatesAreRejected() = runBlocking {
        if (!liveChecksEnabled()) return@runBlocking
        val client = createMyMaiHttpClient()
        try {
            for (url in listOf("https://self-signed.badssl.com/", "https://wrong.host.badssl.com/", "https://expired.badssl.com/")) {
                val error = assertFailsWith<DarwinHttpRequestException>(url) { client.get(url) }
                assertTrue(error.origin.code in -1206L..-1200L, "Expected a certificate failure: $error")
            }
        } finally { client.close() }
    }

    private fun liveChecksEnabled(): Boolean {
        val enabled = NSProcessInfo.processInfo.environment["MAI_TLS_LIVE_TESTS"] == "1"
        if (!enabled) println("Live TLS check skipped; run scripts/check-ios-tls.sh")
        return enabled
    }
}
