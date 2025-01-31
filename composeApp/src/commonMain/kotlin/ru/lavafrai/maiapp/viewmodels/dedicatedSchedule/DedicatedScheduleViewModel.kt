package ru.lavafrai.maiapp.viewmodels.dedicatedSchedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.launch
import ru.lavafrai.maiapp.BuildConfig.API_BASE_URL
import ru.lavafrai.maiapp.data.Loadable
import ru.lavafrai.maiapp.data.repositories.ExlerRepository
import ru.lavafrai.maiapp.data.repositories.ScheduleRepository
import ru.lavafrai.maiapp.models.schedule.ScheduleId
import ru.lavafrai.maiapp.viewmodels.MaiAppViewModel
import ru.lavafrai.maiapp.viewmodels.main.MainPageViewModel
import kotlin.reflect.KClass

class DedicatedScheduleViewModel(
    private val scheduleName: ScheduleId,
): MaiAppViewModel<DedicatedScheduleViewState>(
    DedicatedScheduleViewState(
        schedule = Loadable.loading(),
        exlerTeachers = Loadable.loading(),
    )
) {
    private val scheduleRepository = ScheduleRepository(
        httpClient = httpClient,
        baseUrl = API_BASE_URL,
    )
    private val exlerRepository = ExlerRepository(
        httpClient = httpClient,
        baseUrl = API_BASE_URL,
    )

    init {
        startLoading()
    }

    fun startLoading() = viewModelScope.launch(dispatchers.IO) {
        val cachedSchedule = scheduleRepository.getScheduleFromCacheOrNull(scheduleName)
        if (cachedSchedule != null) {
            emit(stateValue.copy(schedule = Loadable.updating(cachedSchedule)))
        }

        launchCatching(onError = {
            emit(stateValue.copy(schedule = stateValue.schedule.copy(error = it as Exception)))
        }) {
            val schedule = scheduleRepository.getSchedule(scheduleName)
            emit(stateValue.copy(schedule = Loadable.actual(schedule)))
        }

        launchCatching(onError = {
            emit(stateValue.copy(exlerTeachers = stateValue.exlerTeachers.copy(error = it as Exception)))
        }) {
            val exlerTeachers = exlerRepository.getTeachers()
            emit(stateValue.copy(exlerTeachers = Loadable.actual(exlerTeachers)))
        }
    }

    class Factory(
        private val scheduleName: ScheduleId,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
            return DedicatedScheduleViewModel(scheduleName) as T
        }
    }
}