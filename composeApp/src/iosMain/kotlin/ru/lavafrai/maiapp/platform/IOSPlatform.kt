package ru.lavafrai.maiapp.platform

import IosPlatformDependency
import com.russhwolf.settings.Settings
import io.ktor.client.engine.darwin.*
import kotlinx.coroutines.IO
import platform.Foundation.NSURL
import platform.Foundation.NSURLErrorCannotFindHost
import platform.Foundation.NSURLErrorDNSLookupFailed
import platform.Foundation.NSURLErrorDataNotAllowed
import platform.Foundation.NSURLErrorDomain
import platform.Foundation.NSURLErrorNotConnectedToInternet

class IOSPlatform: Platform {
    override fun name() = "iOS"
    override fun ktorEngine() = Darwin
    override fun dispatchers() = Dispatchers(
        IO = kotlinx.coroutines.Dispatchers.IO,
        Main = kotlinx.coroutines.Dispatchers.Main,
        Default = kotlinx.coroutines.Dispatchers.Default
    )
    override fun storage() = Settings()
    override fun openUrl(url: String) {
        IosPlatformDependency.getInstance().openUrl(NSURL(string = url))
    }

    override fun isNoConnectionError(error: Throwable): Boolean {
        val origin = (error as? DarwinHttpRequestException)?.origin ?: return false
        return origin.domain == NSURLErrorDomain && origin.code in listOf(
            NSURLErrorNotConnectedToInternet,
            NSURLErrorDataNotAllowed, // Mobile data is off for the app
            NSURLErrorCannotFindHost,
            NSURLErrorDNSLookupFailed,
        )
    }

    override fun reportError(context: String, error: Throwable, details: String) {
        IosPlatformDependency.getInstance().reportError(
            identifier = "$context: ${error::class.simpleName}",
            message = details,
        )
    }
}