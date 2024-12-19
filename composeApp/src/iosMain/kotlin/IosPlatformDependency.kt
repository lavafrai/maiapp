import platform.Foundation.NSURL

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