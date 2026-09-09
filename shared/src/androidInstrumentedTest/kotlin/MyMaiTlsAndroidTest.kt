package ru.lavafrai.maiapp

import android.security.NetworkSecurityPolicy
import android.net.http.X509TrustManagerExtensions
import java.net.URL
import java.security.KeyStore
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLException
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.*

/** Live TLS integration checks: requires Internet access, no account or password. */
@RunWith(AndroidJUnit4::class)
class MyMaiTlsAndroidTest {
    @Test fun accountHostsAndSystemRootsWork() = runBlocking {
        val client = createMyMaiHttpClient()
        try {
            for (url in listOf("https://esia.mai.ru/auth/realms/lk_mai/", "https://my.mai.ru/", "https://example.com/")) {
                assertTrue(client.get(url).status.value in 200..499, url)
            }
        } finally { client.close() }
    }

    @Test fun invalidCertificatesAreRejected() = runBlocking {
        val client = createMyMaiHttpClient()
        try {
            for (url in listOf("https://self-signed.badssl.com/", "https://wrong.host.badssl.com/", "https://expired.badssl.com/")) {
                val error = assertFails(url) { client.get(url) }
                assertTrue(generateSequence(error) { it.cause }.any {
                    it is SSLException || it is CertificateException ||
                        (it is java.io.IOException && it.message?.contains("Hostname") == true)
                }, "Expected a certificate failure: $error")
            }
        } finally { client.close() }
    }

    @Test fun customRootDoesNotLeakToOtherMaiHosts() {
        val connection = URL("https://esia.mai.ru/").openConnection() as HttpsURLConnection
        val chain = try {
            connection.connect()
            connection.serverCertificates.map { it as X509Certificate }.toTypedArray()
        } finally { connection.disconnect() }
        val manager = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm()).apply {
            init(null as KeyStore?)
        }.trustManagers.filterIsInstance<X509TrustManager>().first()
        val extensions = X509TrustManagerExtensions(manager)
        extensions.checkServerTrusted(chain, "RSA", "esia.mai.ru")
        extensions.checkServerTrusted(chain, "RSA", "my.mai.ru")
        // The wildcard leaf also covers this host, but its CA must not be trusted here.
        assertFailsWith<CertificateException> {
            extensions.checkServerTrusted(chain, "RSA", "other.mai.ru")
        }
    }

    @Test fun accountHostsDisallowCleartext() {
        val policy = NetworkSecurityPolicy.getInstance()
        assertFalse(policy.isCleartextTrafficPermitted("esia.mai.ru"))
        assertFalse(policy.isCleartextTrafficPermitted("my.mai.ru"))
    }
}
