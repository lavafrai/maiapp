package ru.lavafrai.maiapp.viewmodels.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import ru.lavafrai.maiapp.BuildConfig.API_BASE_URL
import ru.lavafrai.maiapp.data.Loadable
import ru.lavafrai.maiapp.data.repositories.ExlerRepository
import ru.lavafrai.maiapp.data.repositories.MaiDataRepository
import ru.lavafrai.maiapp.data.repositories.ScheduleRepository
import ru.lavafrai.maiapp.data.settings.ApplicationSettings
import ru.lavafrai.maiapp.data.settings.VersionInfo
import ru.lavafrai.maiapp.models.schedule.ScheduleId
import ru.lavafrai.maiapp.models.time.DateRange
import ru.lavafrai.maiapp.rootPages.main.MainNavigationPageId
import ru.lavafrai.maiapp.utils.LessonSelector
import ru.lavafrai.maiapp.viewmodels.MaiAppViewModel
import kotlin.reflect.KClass

class MainPageViewModel(
    val onClearSettings: () -> Unit,
    val onShowUpdateInfo: () -> Unit,
) : MaiAppViewModel<MainPageState>(
    initialState = MainPageState(
        page = MainNavigationPageId.HOME,
        schedule = Loadable.loading(),
        selectedWeek = DateRange.currentWeek(),
        workLessonSelectors = listOf(LessonSelector.default()),
        exlerTeachers = Loadable.loading(),
        maidata = Loadable.loading(),
    )
) {
    private var scheduleName: ScheduleId = ApplicationSettings.getCurrent().selectedSchedule!!
    private val scheduleRepository = ScheduleRepository(
        httpClient = httpClient,
        baseUrl = API_BASE_URL
    )
    private val exlerRepository = ExlerRepository(
        httpClient = httpClient,
        baseUrl = API_BASE_URL
    )
    private val maidataRepository = MaiDataRepository(
        httpClient = httpClient,
        baseUrl = API_BASE_URL
    )

    init {
        startLoading()

        if (VersionInfo.hasBeenUpdated()) {
            val lastVersion = VersionInfo.lastVersion
            val currentVersion = VersionInfo.currentVersion

            onVersionUpdated(lastVersion, currentVersion)
        }
    }

    fun setPage(page: MainNavigationPageId) {
        viewModelScope.launch(dispatchers.IO) {
            emit(stateValue.copy(page = page))
        }
    }

    fun reloadSchedule(scheduleId: ScheduleId? = null) {
        viewModelScope.launch(dispatchers.IO) {
            if (scheduleId != null) {
                scheduleName = scheduleId
            }

            /*val scheduleHandler = CoroutineExceptionHandler { _, e ->
                e.printStackTrace()
                emit(stateValue.copy(schedule = stateValue.schedule.copy(error = e as Exception)))
            }

            supervisorScope {
                // Download schedule
                launch(scheduleHandler) {
                    val cachedSchedule = scheduleRepository.getScheduleFromCacheOrNull(scheduleName)
                    if (cachedSchedule != null) emit(stateValue.copy(schedule = Loadable.updating(cachedSchedule)))
                    else emit(stateValue.copy(schedule = Loadable.loading()))
                }.invokeOnCompletion {
                    launch(scheduleHandler) {
                        val schedule = scheduleRepository.getSchedule(scheduleName)
                        emit(stateValue.copy(schedule = Loadable.actual(schedule)))
                    }
                }
            }*/

            launchCatching(
                onError = {
                    emit(stateValue.copy(schedule = stateValue.schedule.copy(error = it as Exception)))
                }
            ) {
                val cachedSchedule = scheduleRepository.getScheduleFromCacheOrNull(scheduleName)
                if (cachedSchedule != null) emit(stateValue.copy(schedule = Loadable.updating(cachedSchedule)))
                else emit(stateValue.copy(schedule = Loadable.loading()))

                val schedule = scheduleRepository.getSchedule(scheduleName)
                emit(stateValue.copy(schedule = Loadable.actual(schedule)))
            }
        }
    }

    fun startLoading() {
        scheduleName = ApplicationSettings.getCurrent().selectedSchedule!!
        viewModelScope.launch(dispatchers.IO) {
            emit(initialState.copy(page = stateValue.page))

            val scheduleHandler = CoroutineExceptionHandler { _, e ->
                e.printStackTrace()
                emit(stateValue.copy(schedule = stateValue.schedule.copy(error = e as Exception)))
            }
            val exlerHandler = CoroutineExceptionHandler { _, e ->
                e.printStackTrace()
                emit(stateValue.copy(exlerTeachers = stateValue.exlerTeachers.copy(error = e as Exception)))
            }
            val maidataHandler = CoroutineExceptionHandler { _, e ->
                e.printStackTrace()
                emit(stateValue.copy(maidata = stateValue.maidata.copy(error = e as Exception)))
            }

            supervisorScope {
                // Download schedule
                launch(scheduleHandler) {
                    val cachedSchedule = scheduleRepository.getScheduleFromCacheOrNull(scheduleName)
                    emit(stateValue.copy(schedule = stateValue.schedule.copy(data = cachedSchedule)))
                }.invokeOnCompletion { launch(scheduleHandler) {
                    val schedule = scheduleRepository.getSchedule(scheduleName)
                    emit(stateValue.copy(schedule = Loadable.actual(schedule)))
                }}

                launch(exlerHandler) {
                    val exlerTeachers = exlerRepository.getTeachers()
                    emit(stateValue.copy(exlerTeachers = Loadable.actual(exlerTeachers)))
                }

                launch(maidataHandler) {
                    val maidata = maidataRepository.getData()
                    emit(stateValue.copy(maidata = Loadable.actual(maidata)))
                }
            }
        }
    }

    fun clearSettings() {
        viewModelScope.launch(dispatchers.IO) {
            ApplicationSettings.clear()
            withContext(dispatchers.Main) {
                onClearSettings()
            }
        }
    }

    fun setTheme(theme: String) {
        viewModelScope.launch(dispatchers.IO) {
            ApplicationSettings.setTheme(theme)
        }
    }

    fun setWeek(dateRange: DateRange) {
        viewModelScope.launch(dispatchers.IO) {
            emit(stateValue.copy(selectedWeek = dateRange))
        }
    }

    fun setWorksLessonSelector(lessonTypes: List<LessonSelector>) {
        viewModelScope.launch(dispatchers.IO) {
            emit(stateValue.copy(workLessonSelectors = lessonTypes))
        }
    }

    fun onVersionUpdated(
        lastVersion: String?,
        currentVersion: String,
    ) {
        if (lastVersion != null) onShowUpdateInfo()
        else VersionInfo.updateLastVersion()
    }

    class Factory(
        private val onClearSettings: () -> Unit,
        private val onShowUpdateInfo: () -> Unit,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
            return MainPageViewModel(
                onClearSettings = onClearSettings,
                onShowUpdateInfo = onShowUpdateInfo,
            ) as T
        }
    }
}