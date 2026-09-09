package ru.lavafrai.maiapp

import java.io.ByteArrayInputStream
import java.net.Socket
import java.security.KeyStore
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.SSLEngine
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509ExtendedTrustManager
import javax.net.ssl.X509TrustManager

/**
 * Custom TrustManager that verifies certificates using the default system trust store,
 * but allows Russian Trusted Root CA ONLY for domains matching "mai.ru" or "*.mai.ru".
 *
 * For all other domains (e.g. google.com, sberbank.ru, evil.com), Russian Trusted Root CA
 * is strictly NOT trusted.
 */
@Suppress("CustomX509TrustManager")
class MaiDomainTrustManager private constructor() : X509ExtendedTrustManager() {

    private val defaultTrustManager: X509TrustManager by lazy {
        val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        tmf.init(null as KeyStore?)
        tmf.trustManagers.filterIsInstance<X509TrustManager>().first()
    }

    private val russianCaTrustManager: X509TrustManager by lazy {
        val certFactory = CertificateFactory.getInstance("X.509")
        val cert = certFactory.generateCertificate(
            ByteArrayInputStream(RUSSIAN_TRUSTED_ROOT_CA_PEM.trimIndent().toByteArray(Charsets.UTF_8))
        ) as X509Certificate
        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType()).apply {
            load(null, null)
            setCertificateEntry("russian_trusted_root_ca", cert)
        }
        val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        tmf.init(keyStore)
        tmf.trustManagers.filterIsInstance<X509TrustManager>().first()
    }

    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?, socket: Socket?) {
        defaultTrustManager.checkClientTrusted(chain, authType)
    }

    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?, engine: SSLEngine?) {
        defaultTrustManager.checkClientTrusted(chain, authType)
    }

    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
        defaultTrustManager.checkClientTrusted(chain, authType)
    }

    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?, socket: Socket?) {
        val host = socket?.inetAddress?.hostName
        verifyServerCertificate(chain, authType, host)
    }

    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?, engine: SSLEngine?) {
        val host = engine?.peerHost
        verifyServerCertificate(chain, authType, host)
    }

    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
        verifyServerCertificate(chain, authType, null)
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> {
        return defaultTrustManager.acceptedIssuers + russianCaTrustManager.acceptedIssuers
    }

    private fun checkSystemTrusted(chain: Array<out X509Certificate>, authType: String?, hostname: String?) {
        // 1. If hostname is available, try hostname-aware checkServerTrusted
        if (hostname != null) {
            try {
                val method = defaultTrustManager.javaClass.getMethod(
                    "checkServerTrusted",
                    Array<X509Certificate>::class.java,
                    String::class.java,
                    String::class.java
                )
                method.invoke(defaultTrustManager, chain, authType, hostname)
                return
            } catch (_: NoSuchMethodException) {
                // Not a hostname-aware trust manager, fall through to 2-arg
            } catch (e: java.lang.reflect.InvocationTargetException) {
                val cause = e.targetException
                if (cause is CertificateException) throw cause
                throw CertificateException(cause)
            }
        }

        // 2. Standard 2-arg checkServerTrusted
        defaultTrustManager.checkServerTrusted(chain, authType)
    }

    private fun verifyServerCertificate(
        chain: Array<out X509Certificate>?,
        authType: String?,
        peerHost: String?
    ) {
        if (chain.isNullOrEmpty()) {
            throw CertificateException("Certificate chain is empty")
        }

        val leaf = chain[0]
        val effectiveHost = peerHost ?: extractPrimaryHost(leaf)

        // 1. First, attempt validation via default system trust store
        try {
            checkSystemTrusted(chain, authType, effectiveHost)
            return
        } catch (systemEx: CertificateException) {
            // System trust manager rejected the certificate chain.
            // Check whether this domain is strictly permitted to use Russian Trusted Root CA.

            // If effectiveHost is known, it must be mai.ru or *.mai.ru
            if (effectiveHost != null && !isMaiDomain(effectiveHost)) {
                throw CertificateException(
                    "Certificate is not trusted by system CAs, and Russian Root CA is only trusted for mai.ru domains (host: $effectiveHost)",
                    systemEx
                )
            }

            // The leaf certificate must strictly belong to mai.ru or *.mai.ru
            if (!isCertificateForMaiRu(leaf)) {
                throw CertificateException(
                    "Certificate is not trusted by system CAs, and certificate Subject/SAN does not match mai.ru domains",
                    systemEx
                )
            }

            // 2. Validate chain against Russian Trusted Root CA
            try {
                russianCaTrustManager.checkServerTrusted(chain, authType)
            } catch (caEx: CertificateException) {
                throw CertificateException(
                    "Validation for mai.ru failed with Russian Trusted Root CA: ${caEx.message}",
                    caEx
                )
            }
        }
    }

    private fun isMaiDomain(host: String): Boolean {
        val clean = host.trim().lowercase().removeSuffix(".")
        return clean == "mai.ru" || clean.endsWith(".mai.ru")
    }

    private fun extractPrimaryHost(cert: X509Certificate): String? {
        try {
            cert.subjectAlternativeNames?.forEach { san ->
                if (san.size >= 2 && san[0] == 2) {
                    (san[1] as? String)?.let {
                        return it.trim().lowercase().removePrefix("*.").removeSuffix(".")
                    }
                }
            }
        } catch (_: Exception) {}

        val principal = cert.subjectX500Principal.name
        return Regex("""(?:^|,)\s*CN=([^,]+)""").find(principal)?.groupValues?.get(1)?.trim()
            ?.lowercase()?.removePrefix("*.")?.removeSuffix(".")
    }

    private fun isCertificateForMaiRu(cert: X509Certificate): Boolean {
        val dnsNames = mutableListOf<String>()
        try {
            cert.subjectAlternativeNames?.forEach { san ->
                if (san.size >= 2 && san[0] == 2) {
                    (san[1] as? String)?.let { dnsNames.add(it) }
                }
            }
        } catch (_: Exception) {}

        val principal = cert.subjectX500Principal.name
        Regex("""(?:^|,)\s*CN=([^,]+)""").find(principal)?.groupValues?.get(1)?.trim()?.let {
            dnsNames.add(it)
        }

        if (dnsNames.isEmpty()) return false

        return dnsNames.all { name ->
            val clean = name.trim().lowercase().removePrefix("*.").removeSuffix(".")
            clean == "mai.ru" || clean.endsWith(".mai.ru")
        }
    }

    companion object {
        val instance = MaiDomainTrustManager()
    }
}
