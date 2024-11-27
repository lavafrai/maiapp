package ru.lavafrai.maiapp.viewmodels.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import ru.lavafrai.maiapp.BuildConfig.API_BASE_URL
import ru.lavafrai.maiapp.data.Loadable
import ru.lavafrai.maiapp.data.repositories.ScheduleRepository
import ru.lavafrai.maiapp.data.settings.ApplicationSettings
import ru.lavafrai.maiapp.models.time.DateRange
import ru.lavafrai.maiapp.viewmodels.MaiAppViewModel
import kotlin.reflect.KClass

class MainPageViewModel(
    val onClearSettings: () -> Unit
) : MaiAppViewModel<MainPageState>(
    initialState = MainPageState(
        schedule = Loadable.loading(),
        selectedWeek = DateRange.currentWeek()
    )
) {
    val scheduleName = ApplicationSettings.getCurrent().selectedSchedule!!
    val scheduleRepository = ScheduleRepository(
        httpClient = httpClient,
        baseUrl = API_BASE_URL
    )

    init {
        startLoading()
    }

    fun startLoading() {
        viewModelScope.launch(dispatchers.IO) {
            emit(initialState)

            // Download schedule
            supervisorScope {
                try {
                    val schedule = scheduleRepository.getSchedule(scheduleName)
                    emit(stateValue.copy(schedule = Loadable.actual(schedule)))
                } catch (e: Exception) {
                    e.printStackTrace()
                    emit(stateValue.copy(schedule = Loadable.error(e)))
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

    class Factory(
        private val onClearSettings: () -> Unit
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
            return MainPageViewModel(
                onClearSettings = onClearSettings,
            ) as T
        }
    }
}