package ru.lavafrai.maiapp.viewmodels

import androidx.lifecycle.viewModelScope
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import ru.lavafrai.maiapp.data.Loadable
import ru.lavafrai.maiapp.models.group.Group
import ru.lavafrai.maiapp.models.schedule.TeacherId

class LoginPageViewModel: MaiAppViewModel() {
    private val _state = MutableStateFlow(LoginPageState(groups = Loadable.loading(), teachers = Loadable.loading()))
    val state: StateFlow<LoginPageState> = _state

    init {
        startLoading()
    }

    fun startLoading() {
        viewModelScope.launch {
            supervisorScope {
                val groupsTask = async { httpClient.get("/groups").body<List<Group>>() }
                val teachersTask = async { httpClient.get("/teachers").body<List<TeacherId>>() }

                try {
                    val groups = groupsTask.await()
                    _state.value = _state.value.copy(groups = Loadable.actual(groups))
                } catch (e: Exception) {
                    e.printStackTrace()
                    _state.value = _state.value.copy(groups = Loadable.error(e))
                }

                try {
                    val teachers = teachersTask.await()
                    _state.value = _state.value.copy(teachers = Loadable.actual(teachers))
                } catch (e: Exception) {
                    e.printStackTrace()
                    _state.value = _state.value.copy(teachers = Loadable.error(e))
                }
            }
        }
    }
}