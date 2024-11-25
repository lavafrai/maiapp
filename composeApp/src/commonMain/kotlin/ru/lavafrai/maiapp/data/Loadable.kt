package ru.lavafrai.maiapp.data

data class Loadable<T>(
    val data: T?,
    val error: Exception?,
    val actual: Boolean,
) {
    fun hasData() = data != null
    fun hasError() = error != null

    val status: LoadableStatus
        get() = when {
            !hasData() && hasError() -> LoadableStatus.Error
            !hasData() && !hasError() -> LoadableStatus.Loading
            hasData() && !actual && !hasError() -> LoadableStatus.Updating
            hasData() && actual -> LoadableStatus.Actual
            hasData() && hasError() -> LoadableStatus.Offline
            else -> throw IllegalStateException("Invalid Loadable state")
        }

    val baseStatus: BaseLoadableStatus
        get() = when(status) {
            LoadableStatus.Error -> BaseLoadableStatus.Error
            LoadableStatus.Loading -> BaseLoadableStatus.Loading
            LoadableStatus.Actual -> BaseLoadableStatus.Actual
            LoadableStatus.Offline -> BaseLoadableStatus.Actual
            LoadableStatus.Updating -> BaseLoadableStatus.Actual
        }

    companion object {
        fun <T> loading() = Loadable<T>(null, null, false)
        fun <T> error(error: Exception) = Loadable<T>(null, error, false)
        fun <T> actual(data: T) = Loadable(data, null, true)
        fun <T> updating(data: T) = Loadable(data, null, false)
        fun <T> offline(data: T) = Loadable(data, null, false)
    }
}

enum class LoadableStatus {
    Error,      // failed to load anything, no cached data
    Loading,    // loading data, no cached value
    Updating,  // data from cache, but loading new data
    Actual,     // actual data, received from server
    Offline,    // data from cache, failed to update
}

enum class BaseLoadableStatus {
    Error,
    Loading,
    Actual,
}