package ru.lavafrai.maiapp

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.darwin.*
import kotlinx.cinterop.*
import platform.CoreFoundation.*
import platform.Foundation.*
import platform.Security.*

actual fun platformHttpClientProvider(): HttpClientEngineFactory<*> = Darwin

@OptIn(ExperimentalForeignApi::class)
private inline fun <T : CPointed, R> CPointer<T>.useCF(block: (CPointer<T>) -> R): R =
    try {
        block(this)
    } finally {
        CFRelease(this)
    }

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private val russianRootCertificate: SecCertificateRef by lazy {
    val base64 = RUSSIAN_TRUSTED_ROOT_CA_PEM.lines()
        .filterNot { it.startsWith("-----") }
        .joinToString("")
    val data = checkNotNull(NSData.create(base64EncodedString = base64, options = 0u))
    val cfData = checkNotNull(CFBridgingRetain(data)).reinterpret<cnames.structs.__CFData>()
    // Retain this certificate for the lifetime of the process; each trust retains its anchors.
    cfData.useCF { checkNotNull(SecCertificateCreateWithData(null, it)) }
}

@OptIn(ExperimentalForeignApi::class)
private fun evaluateMyMaiTrust(trust: SecTrustRef, host: String): Boolean = memScoped {
    val hostname = CFStringCreateWithCString(null, host, kCFStringEncodingUTF8)
        ?: return@memScoped false
    val policyStatus = hostname.useCF { name ->
        val policy = SecPolicyCreateSSL(true, name) ?: return@memScoped false
        policy.useCF { SecTrustSetPolicies(trust, it) }
    }
    if (policyStatus != errSecSuccess) return@memScoped false

    val certificate = alloc<SecCertificateRefVar>()
    certificate.value = russianRootCertificate
    val anchors = CFArrayCreate(null, certificate.ptr.reinterpret(), 1L, kCFTypeArrayCallBacks.ptr)
        ?: return@memScoped false
    anchors.useCF {
        if (SecTrustSetAnchorCertificates(trust, it) != errSecSuccess) return@memScoped false
    }
    if (SecTrustSetAnchorCertificatesOnly(trust, false) != errSecSuccess) return@memScoped false
    return@memScoped SecTrustEvaluateWithError(trust, null)
}

@OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
actual fun createMyMaiHttpClient(): HttpClient = HttpClient(Darwin) {
    engine {
        handleChallenge { _, _, challenge, complete ->
            val space = challenge.protectionSpace
            if (space.authenticationMethod != NSURLAuthenticationMethodServerTrust || !isMyMaiHost(space.host)) {
                complete(NSURLSessionAuthChallengePerformDefaultHandling, null)
                return@handleChallenge
            }
            val trust = space.serverTrust
            if (trust != null && evaluateMyMaiTrust(trust, space.host)) {
                complete(NSURLSessionAuthChallengeUseCredential, NSURLCredential.credentialForTrust(trust))
            } else {
                complete(NSURLSessionAuthChallengeCancelAuthenticationChallenge, null)
            }
        }
    }
    configureMyMaiClient()
}
