import platform.Foundation.NSURL
import ru.lavafrai.maiapp.BuildConfig.APPMETRICA_APIKEY

interface IosPlatformDependency {
    fun openUrl(url: NSURL)

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