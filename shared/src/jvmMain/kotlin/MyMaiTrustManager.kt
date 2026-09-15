package ru.lavafrai.maiapp

import java.security.KeyStore
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

// Used only on connections to the exact My MAI hosts. HttpsURLConnection verifies
// the requested hostname separately; never infer it from a server certificate.
internal class MyMaiTrustManager(
    private val system: X509TrustManager,
    private val additional: X509TrustManager,
) : X509TrustManager {
    override fun checkServerTrusted(chain: Array<out X509Certificate>, authType: String) {
        try {
            system.checkServerTrusted(chain, authType)
        } catch (systemError: CertificateException) {
            try {
                additional.checkServerTrusted(chain, authType)
            } catch (error: CertificateException) {
                error.addSuppressed(systemError)
                throw error
            }
        }
    }

    override fun checkClientTrusted(chain: Array<out X509Certificate>, authType: String) =
        system.checkClientTrusted(chain, authType)

    override fun getAcceptedIssuers(): Array<X509Certificate> =
        system.acceptedIssuers + additional.acceptedIssuers
}

private fun trustManager(store: KeyStore?): X509TrustManager =
    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm()).apply {
        init(store)
    }.trustManagers.filterIsInstance<X509TrustManager>().first()

internal val myMaiSocketFactory by lazy {
    val certificate = RUSSIAN_TRUSTED_ROOT_CA_PEM.byteInputStream().use {
        CertificateFactory.getInstance("X.509").generateCertificate(it)
    }
    val store = KeyStore.getInstance(KeyStore.getDefaultType()).apply {
        load(null, null)
        setCertificateEntry("russian_trusted_root_ca", certificate)
    }
    SSLContext.getInstance("TLS").apply {
        init(null, arrayOf(MyMaiTrustManager(trustManager(null), trustManager(store))), null)
    }.socketFactory
}
