package ru.lavafrai.maiapp.viewmodels.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import ru.lavafrai.maiapp.BuildConfig.API_BASE_URL
import ru.lavafrai.maiapp.data.Loadable
import ru.lavafrai.maiapp.data.settings.ApplicationSettings
import ru.lavafrai.maiapp.models.Nameable
import ru.lavafrai.maiapp.navigation.Pages
import ru.lavafrai.maiapp.network.MaiApi
import ru.lavafrai.maiapp.viewmodels.MaiAppViewModel
import kotlin.reflect.KClass

class LoginPageViewModel(
    val loginData: Pages.Login,
    val onNavigateBack: () -> Unit,
    val onLoginDone: (Nameable) -> Unit,
) : MaiAppViewModel<LoginPageState>(
    initialState = LoginPageState(
        groups = Loadable.loading(),
        teachers = Loadable.loading(),
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

            supervisorScope {
                val groupsTask = async { api.groups() }
                val teachersTask = async { api.teachers() }

                try {
                    val groups = groupsTask.await()
                    emit(stateValue.copy(groups = Loadable.actual(groups)))
                } catch (e: Exception) {
                    e.printStackTrace()
                    emit(stateValue.copy(groups = Loadable.error(e)))
                }

                try {
                    val teachers = teachersTask.await()
                    emit(stateValue.copy(teachers = Loadable.actual(teachers)))
                } catch (e: Exception) {
                    e.printStackTrace()
                    emit(stateValue.copy(teachers = Loadable.error(e)))
                }

                updateSearchQuery("")
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
        if (loginData.target == LoginTarget.ADD_SCHEDULE) {
            viewModelScope.launch(dispatchers.Default) {
                ApplicationSettings.setSelectedGroup(selected.name)
            }
        }
    }

    fun navigateBack() {
        onNavigateBack()
    }

    class Factory(
        private val loginData: Pages.Login,
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


