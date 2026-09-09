package ru.lavafrai.maiapp

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.darwin.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.cinterop.*
import platform.CoreFoundation.*
import platform.Foundation.*
import platform.Security.*

actual fun platformHttpClientProvider(): HttpClientEngineFactory<*> = Darwin

private fun isMaiDomain(host: String): Boolean {
    val clean = host.lowercase().trimEnd('.')
    return clean == "mai.ru" || clean.endsWith(".mai.ru")
}

@OptIn(ExperimentalForeignApi::class)
private inline fun <T : CPointed, R> CPointer<T>.use(block: (CPointer<T>) -> R): R {
    try {
        return block(this)
    } finally {
        CFBridgingRelease(this)
    }
}

@OptIn(ExperimentalForeignApi::class)
private val russianRootCertificate: SecCertificateRef? by lazy {
    val cleanBase64 = RUSSIAN_TRUSTED_ROOT_CA_PEM
        .lines()
        .filterNot { it.startsWith("-----") }
        .joinToString("")
        .trim()

    val certData = NSData.create(base64EncodedString = cleanBase64, options = 0u) ?: return@lazy null
    val cfData = CFBridgingRetain(certData)?.reinterpret<cnames.structs.__CFData>() ?: return@lazy null
    cfData.use {
        SecCertificateCreateWithData(null, it)
    }
}

@OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
actual fun createMyMaiHttpClient(): HttpClient = HttpClient(Darwin) {
    engine {
        handleChallenge { _, _, challenge, completionHandler ->
            val host = challenge.protectionSpace.host
            if (challenge.protectionSpace.authenticationMethod != NSURLAuthenticationMethodServerTrust || !isMaiDomain(host)) {
                completionHandler(NSURLSessionAuthChallengePerformDefaultHandling, null)
                return@handleChallenge
            }

            val trust = challenge.protectionSpace.serverTrust
            val cert = russianRootCertificate
            if (trust == null || cert == null) {
                completionHandler(NSURLSessionAuthChallengePerformDefaultHandling, null)
                return@handleChallenge
            }

            memScoped {
                val hostCFString = CFStringCreateWithCString(null, host, kCFStringEncodingUTF8)
                hostCFString?.use { hostStr ->
                    SecPolicyCreateSSL(true, hostStr)?.use { policy ->
                        SecTrustSetPolicies(trust, policy)
                    }
                }

                val certPtr = alloc<SecCertificateRefVar>()
                certPtr.value = cert

                var trusted = false
                val anchors = CFArrayCreate(
                    null,
                    certPtr.ptr.reinterpret(),
                    1L,
                    kCFTypeArrayCallBacks.ptr
                )
                anchors?.use { array ->
                    SecTrustSetAnchorCertificates(trust, array)
                    SecTrustSetAnchorCertificatesOnly(trust, false)
                    trusted = SecTrustEvaluateWithError(trust, null)
                }

                if (trusted) {
                    completionHandler(
                        NSURLSessionAuthChallengeUseCredential,
                        NSURLCredential.credentialForTrust(trust)
                    )
                } else {
                    completionHandler(NSURLSessionAuthChallengeCancelAuthenticationChallenge, null)
                }
            }
        }
    }
    install(ContentNegotiation) {
        json(JsonProvider.tolerantJson)
    }
}