package ru.lavafrai.maiapp.viewmodels.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.*
import ru.lavafrai.maiapp.BuildConfig.API_BASE_URL
import ru.lavafrai.maiapp.data.Loadable
import ru.lavafrai.maiapp.data.repositories.ExlerRepository
import ru.lavafrai.maiapp.data.repositories.ScheduleRepository
import ru.lavafrai.maiapp.data.settings.ApplicationSettings
import ru.lavafrai.maiapp.data.settings.VersionInfo
import ru.lavafrai.maiapp.models.schedule.LessonType
import ru.lavafrai.maiapp.models.time.DateRange
import ru.lavafrai.maiapp.rootPages.main.MainNavigationPageId
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
        workTypeSelected = listOf(
            LessonType.LABORATORY,
            LessonType.EXAM,
        ),
        exlerTeachers = Loadable.loading()
    )
) {
    private val scheduleName = ApplicationSettings.getCurrent().selectedSchedule!!
    private val scheduleRepository = ScheduleRepository(
        httpClient = httpClient,
        baseUrl = API_BASE_URL
    )
    private val exlerRepository = ExlerRepository(
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

    fun startLoading() {
        viewModelScope.launch(dispatchers.IO) {
            emit(initialState)

            val scheduleHandler = CoroutineExceptionHandler { _, e ->
                e.printStackTrace()
                emit(stateValue.copy(schedule = stateValue.schedule.copy(error = e as Exception)))
            }
            val exlerHandler = CoroutineExceptionHandler { _, e ->
                e.printStackTrace()
                emit(stateValue.copy(exlerTeachers = stateValue.exlerTeachers.copy(error = e as Exception)))
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

    fun setSelectedWorkTypes(lessonTypes: List<LessonType>) {
        viewModelScope.launch(dispatchers.IO) {
            emit(stateValue.copy(workTypeSelected = lessonTypes))
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