import platform.Foundation.NSURL
import ru.lavafrai.maiapp.BuildConfig.APPMETRICA_APIKEY

interface IosPlatformDependency {
    fun openUrl(url: NSURL)

    /** Reports a handled error to AppMetrica, which is only available from Swift */
    fun reportError(identifier: String, message: String)

    companion object {
        var instance: IosPlatformDependency? = null

        fun getInstance(): IosPlatformDependency {
            return instance ?: error("App not initialized yet")
        }
    }
}

fun setInstance(new: IosPlatformDependency) {
    IosPlatformDependency.instance = new
}

fun getAppmetricaKey(): String {
    return APPMETRICA_APIKEY
}