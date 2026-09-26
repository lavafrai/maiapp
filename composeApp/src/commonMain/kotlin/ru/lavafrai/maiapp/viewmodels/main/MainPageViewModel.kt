package ru.lavafrai.maiapp.viewmodels.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import ru.lavafrai.maiapp.BuildConfig.API_BASE_URL
import ru.lavafrai.maiapp.data.Loadable
import ru.lavafrai.maiapp.data.repositories.EventRepository
import ru.lavafrai.maiapp.data.repositories.ExlerRepository
import ru.lavafrai.maiapp.data.repositories.MaiDataRepository
import ru.lavafrai.maiapp.data.repositories.ScheduleRepository
import ru.lavafrai.maiapp.data.settings.ApplicationSettings
import ru.lavafrai.maiapp.data.settings.VersionInfo
import ru.lavafrai.maiapp.models.events.Event
import ru.lavafrai.maiapp.models.events.SimpleEvent
import ru.lavafrai.maiapp.models.schedule.Schedule
import ru.lavafrai.maiapp.models.schedule.ScheduleId
import ru.lavafrai.maiapp.models.schedule.defaultWeek
import ru.lavafrai.maiapp.models.time.DateRange
import ru.lavafrai.maiapp.rootPages.main.MainNavigationPageId
import ru.lavafrai.maiapp.utils.LessonSelector
import ru.lavafrai.maiapp.viewmodels.MaiAppViewModel
import kotlin.concurrent.Volatile
import kotlin.reflect.KClass
import kotlin.uuid.Uuid

class MainPageViewModel(
    val onClearSettings: () -> Unit,
    val onShowUpdateInfo: () -> Unit,
) : MaiAppViewModel<MainPageState>(
    initialState = MainPageState(
        page = MainNavigationPageId.HOME,
        schedule = Loadable.loading(),
        events = Loadable.loading(),
        selectedWeek = DateRange.currentWeek(),
        workLessonSelectors = listOf(LessonSelector.default()),
        exlerTeachers = Loadable.loading(),
        maidata = Loadable.loading(),
    )
) {
    @Volatile private var scheduleName: ScheduleId = ApplicationSettings.getCurrent().selectedSchedule!!
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
    private val eventRepository = EventRepository
    private var scheduleLoading: Job? = null

    init {
        _instance = this
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

    /**
     * @param restartIfLoading if false and this schedule is already being loaded, waits for that loading instead of
     * starting a new one (e.g. to not load the schedule twice on start)
     */
    fun reloadSchedule(
        scheduleId: ScheduleId? = null,
        restartIfLoading: Boolean = true,
        onReloaded: (() -> Unit)? = null,
    ) {
        val name = scheduleId ?: scheduleName
        val loading = scheduleLoading
        if (!restartIfLoading && name == scheduleName && loading != null && loading.isActive) {
            // That loading may have read events before they were changed (e.g. in the events editor); they're local and cheap
            viewModelScope.launch { reloadEvents() }
            onReloaded?.let { loading.invokeOnMainWhenDone(it) }
            return
        }

        val scheduleChanged = name != scheduleName
        scheduleName = name
        launchScheduleLoading(
            // Data of the previous schedule must be neither shown nor used to pick the week for this one
            reset = if (scheduleChanged) ({ copy(schedule = Loadable.loading(), events = Loadable.loading(), weekChosen = false) }) else null,
            onLoaded = onReloaded,
        )
    }

    /**
     * Loads the schedule [scheduleName] and its events, picking the default week if it isn't chosen yet.
     * The previous loading is cancelled, so a late response for a previously selected schedule can't overwrite this one.
     *
     * @param reset applied to the state right away, so neither a cancelled loading nor a quick next call can skip it
     */
    private fun launchScheduleLoading(
        reset: (MainPageState.() -> MainPageState)? = null,
        onLoaded: (() -> Unit)? = null,
    ) {
        scheduleLoading?.cancel()
        reset?.let { emit(stateValue.it()) }
        val name = scheduleName

        scheduleLoading = viewModelScope.launch(dispatchers.IO) {
            try {
                val cachedSchedule = scheduleRepository.getScheduleFromCacheOrNull(name)
                // listAllEvents() handles its errors itself
                val events = eventRepository.listAllEvents(name)
                val cachedWeek = defaultWeekOrNull(cachedSchedule, events)
                ensureActive()
                // Together with the week, so the schedule isn't shown on a wrong week first
                emit(stateValue.copy(
                    schedule = if (cachedSchedule != null) Loadable.updating(cachedSchedule) else Loadable.loading(),
                    events = Loadable.actual(events),
                ).withDefaultWeek(cachedWeek))

                val schedule = scheduleRepository.getSchedule(name)
                val week = defaultWeekOrNull(schedule, events)
                ensureActive()
                emit(stateValue.copy(schedule = Loadable.actual(schedule)).withDefaultWeek(week))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // A cancelled loading must not put its error into the state of the next one
                ensureActive()
                e.printStackTrace()
                emit(stateValue.copy(schedule = stateValue.schedule.copy(error = e)))
            }
        }.also { job -> onLoaded?.let { job.invokeOnMainWhenDone(it) } }
    }

    /** Null if the week is already chosen: it's picked once per loaded schedule, so it doesn't jump on refresh */
    private fun defaultWeekOrNull(schedule: Schedule?, events: List<Event>): DateRange? {
        if (schedule == null || stateValue.weekChosen) return null
        return try {
            val selector = LessonSelector.mainSchedule(ApplicationSettings.getCurrent())
            schedule.defaultWeek(events) { selector.test(it.date, it, emptyList()) }
        } catch (e: Exception) {
            // Not worth failing the schedule loading over, the current week stays
            e.printStackTrace()
            null
        }
    }

    private fun MainPageState.withDefaultWeek(week: DateRange?): MainPageState {
        if (week == null || weekChosen) return this
        return copy(selectedWeek = week, weekChosen = true)
    }

    private fun Job.invokeOnMainWhenDone(block: () -> Unit) {
        invokeOnCompletion { viewModelScope.launch(dispatchers.Main) { block() } }
    }

    suspend fun reloadEvents() {
        withContext(dispatchers.IO) {
            // listAllEvents() handles its errors itself
            val events = eventRepository.listAllEvents(scheduleName)
            emit(stateValue.copy(events = Loadable.actual(events)))
        }
    }

    fun startLoading() {
        scheduleName = ApplicationSettings.getCurrent().selectedSchedule!!
        launchScheduleLoading(reset = { initialState.copy(page = page) })

        viewModelScope.launch(dispatchers.IO) {
            val exlerHandler = CoroutineExceptionHandler { _, e ->
                e.printStackTrace()
                emit(stateValue.copy(exlerTeachers = stateValue.exlerTeachers.copy(error = e as Exception)))
            }
            val maidataHandler = CoroutineExceptionHandler { _, e ->
                e.printStackTrace()
                emit(stateValue.copy(maidata = stateValue.maidata.copy(error = e as Exception)))
            }

            supervisorScope {
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
            emit(stateValue.copy(selectedWeek = dateRange, weekChosen = true))
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

    fun createSimpleEvent(
        event: SimpleEvent,
    ) {
        viewModelScope.launch(dispatchers.IO) {
            try {
                eventRepository.createEvent(event, scheduleName)
                reloadEvents()
            } catch (e: Exception) {
                e.printStackTrace()
                emit(stateValue.copy(events = stateValue.events.copy(error = e)))
            }
        }
    }

    fun deleteEvent(
        eventId: Uuid,
    ) {
        viewModelScope.launch(dispatchers.IO) {
            try {
                eventRepository.deleteEvent(eventId)
                reloadEvents()
            } catch (e: Exception) {
                e.printStackTrace()
                emit(stateValue.copy(events = stateValue.events.copy(error = e)))
            } finally {
                reloadEvents()
            }
        }
    }

    companion object {
        private var _instance: MainPageViewModel? = null

        fun getInstance(): MainPageViewModel {
            return _instance ?: throw IllegalStateException("MainPageViewModel is not initialized")
        }
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