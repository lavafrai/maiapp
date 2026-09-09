package ru.lavafrai.maiapp

import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import org.junit.Assume.assumeTrue
import javax.net.ssl.SSLException
import kotlin.test.*

/** Opt in with MAI_TLS_LIVE_TESTS=1; requires Internet, never submits credentials. */
class MyMaiTlsJvmIntegrationTest {
    @Test fun accountHostsAndSystemRootsWork() = runBlocking {
        assumeTrue(System.getenv("MAI_TLS_LIVE_TESTS") == "1")
        val client = createMyMaiHttpClient()
        try {
            for (url in listOf("https://esia.mai.ru/auth/realms/lk_mai/", "https://my.mai.ru/", "https://example.com/")) {
                assertTrue(client.get(url).status.value in 200..499, url)
            }
        } finally { client.close() }
    }

    @Test fun invalidCertificatesAndOtherMaiHostsAreRejected() = runBlocking {
        assumeTrue(System.getenv("MAI_TLS_LIVE_TESTS") == "1")
        val client = createMyMaiHttpClient()
        try {
            for (url in listOf("https://self-signed.badssl.com/", "https://wrong.host.badssl.com/", "https://expired.badssl.com/", "https://mai.ru/")) {
                val error = assertFails(url) { client.get(url) }
                assertTrue(generateSequence(error) { it.cause }.any { it is SSLException },
                    "Expected a certificate failure: $error")
            }
        } finally { client.close() }
    }
}
