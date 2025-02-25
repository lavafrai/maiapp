package ru.lavafrai.maiapp.viewmodels.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.launch
import ru.lavafrai.maiapp.BuildConfig.API_BASE_URL
import ru.lavafrai.maiapp.data.Loadable
import ru.lavafrai.maiapp.data.settings.ApplicationSettings
import ru.lavafrai.maiapp.models.Nameable
import ru.lavafrai.maiapp.models.schedule.BaseScheduleId
import ru.lavafrai.maiapp.navigation.pages.LoginPage
import ru.lavafrai.maiapp.network.MaiApi
import ru.lavafrai.maiapp.viewmodels.MaiAppViewModel
import kotlin.reflect.KClass

class LoginPageViewModel(
    val loginData: LoginPage,
    val onNavigateBack: () -> Unit,
    val onLoginDone: (Nameable) -> Unit,
) : MaiAppViewModel<LoginPageState>(
    initialState = LoginPageState(
        groups = Loadable.loading(),
        teachers = Loadable.loading(),
        exler = Loadable.loading(),
        filteredData = emptyList(),
        loginData = loginData,
        selected = null
    )
) {
    val api = MaiApi(
        httpClient = httpClient,
        baseUrl = API_BASE_URL,
    )

    init {
        startLoading()
    }

    fun startLoading() {
        viewModelScope.launch(dispatchers.IO) {
            emit(initialState)

            launchCatching(onError = { e -> emit(stateValue.copy(groups = Loadable.error(e))) }) {
                val groups = api.groups()
                emit(stateValue.copy(groups = Loadable.actual(groups)))
                if (loginData.type == LoginType.STUDENT) updateSearchQuery("")
            }

            launchCatching(onError = { e -> emit(stateValue.copy(teachers = Loadable.error(e))) }) {
                val teachers = api.teachers()
                emit(stateValue.copy(teachers = Loadable.actual(teachers)))
                if (loginData.type == LoginType.TEACHER) updateSearchQuery("")
            }

            launchCatching(onError = { e -> emit(stateValue.copy(exler = Loadable.error(e))) }) {
                val exler = api.exlerTeachers()
                emit(stateValue.copy(exler = Loadable.actual(exler)))
                if (loginData.type == LoginType.EXLER) updateSearchQuery("")
            }
        }
    }

    fun updateSearchQuery(query: String) {
        if (stateValue.neededLoadable.hasData()) viewModelScope.launch(dispatchers.Default) {
            val filtered = stateValue.neededLoadable.data!!.filter {
                normalizeQuery(it.name).contains(normalizeQuery(query))
            }
            emit(stateValue.copy(filteredData = filtered))
        }
        return
    }

    private fun normalizeQuery(query: String): String {
        return query
            .replace("-", "")
            .lowercase()
    }

    fun select(selected: Nameable) {
        if (loginData.navigateImmediately) {
            doFinishAction(selected)
            onLoginDone(selected)
        }
        else {
            viewModelScope.launch(dispatchers.Default) {
                emit(stateValue.copy(selected = selected))
            }
        }
    }

    fun login() {
        if (stateValue.selected == null) throw IllegalStateException("No selected item")
        doFinishAction(stateValue.selected!!)
        onLoginDone(stateValue.selected!!)
    }

    private fun doFinishAction(selected: Nameable) {
        when (loginData.target) {
            LoginTarget.ADD_SCHEDULE -> {
                viewModelScope.launch(dispatchers.Default) {
                    ApplicationSettings.addSavedGroup(BaseScheduleId(selected.name))
                    ApplicationSettings.setSelectedGroup(BaseScheduleId(selected.name))
                }
            }
            LoginTarget.OPEN_SCHEDULE -> {

            }
        }
    }

    fun navigateBack() {
        onNavigateBack()
    }

    class Factory(
        private val loginData: LoginPage,
        val onNavigateBack: () -> Unit,
        val onLoginDone: (Nameable) -> Unit,
    ): ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
            return LoginPageViewModel(
                loginData = loginData,
                onNavigateBack = onNavigateBack,
                onLoginDone = onLoginDone,
            ) as T
        }
    }
}


