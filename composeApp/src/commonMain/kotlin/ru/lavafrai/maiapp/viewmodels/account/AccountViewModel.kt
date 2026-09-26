package ru.lavafrai.maiapp.viewmodels.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.io.IOException
import ru.lavafrai.maiapp.data.Loadable
import ru.lavafrai.maiapp.data.repositories.AccountRepository
import ru.lavafrai.maiapp.data.settings.ApplicationSettings
import ru.lavafrai.maiapp.models.account.Student
import ru.lavafrai.maiapp.network.mymai.MyMaiApi
import ru.lavafrai.maiapp.network.mymai.exceptions.AuthenticationServerException
import ru.lavafrai.maiapp.network.mymai.exceptions.InvalidLoginOrPasswordException
import ru.lavafrai.maiapp.utils.contextual
import ru.lavafrai.maiapp.utils.isNoConnectionError
import ru.lavafrai.maiapp.utils.reportError
import ru.lavafrai.maiapp.viewmodels.MaiAppViewModel
import kotlin.reflect.KClass

class AccountViewModel(
    private val accountRepository: AccountRepository = AccountRepository(),
): MaiAppViewModel<AccountViewState>(
    initialState = AccountViewState(
        loggedIn = accountRepository.hasCredentials(),
        studentInfo = Loadable.loading(),
        student = Loadable.loading(),
        marks = Loadable.loading(),
    )
) {
    private var accountLoading: Job? = null
    private var marksReloading: Job? = null

    init {
        refresh()
    }

    fun refresh() {
        accountLoading?.cancel()
        marksReloading?.cancel()
        emit(initialState.copy(loggedIn = accountRepository.hasCredentials()))

        val credentials = accountRepository.getCredentials() ?: return

        accountLoading = viewModelScope.launch {
            val (session, studentInfo) = try {
                val session = MyMaiApi.authorize(credentials.login, credentials.password)
                session to session.studentInfo()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                reportLoadingError("MyMai account loading", e)
                emit(stateValue.copy(studentInfo = Loadable.error(e)))
                return@launch
            }
            emit(stateValue.copy(studentInfo = Loadable.actual(studentInfo)))

            ApplicationSettings.state.map { it.selectedStudentId }.distinctUntilChanged().collectLatest { selectedId ->
                val student = studentInfo.students.firstOrNull { it.id == selectedId } ?: studentInfo.students.firstOrNull()
                emit(stateValue.copy(student = Loadable.actual(student), marks = Loadable.loading()))
                // No students means an unsupported account, the page shows it by itself
                if (student == null) return@collectLatest

                try {
                    val marks = session.studentMarks(student.studentCode)
                    emit(stateValue.copy(marks = Loadable.actual(marks)))
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    reportLoadingError("MyMai marks loading", e)
                    emit(stateValue.copy(marks = Loadable.error(e)))
                }
            }
        }
    }

    fun reloadMarks(student: Student) {
        val credentials = accountRepository.getCredentials() ?: return
        marksReloading?.cancel()
        marksReloading = viewModelScope.launch {
            emit(stateValue.copy(marks = Loadable.loading()))
            try {
                val session = MyMaiApi.authorize(credentials.login, credentials.password)
                val marks = session.studentMarks(student.studentCode)
                emit(stateValue.copy(marks = Loadable.actual(marks)))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                reportLoadingError("MyMai marks loading", e)
                emit(stateValue.copy(marks = Loadable.error(e)))
            }
        }
    }

    /**
     * Unlike sign in, loading runs by itself (e.g. on every start), so network problems are expected there
     * and would flood the reports; a wrong password isn't an app error either
     */
    private fun reportLoadingError(context: String, e: Exception) {
        if (e is IOException || e.isNoConnectionError() || e is InvalidLoginOrPasswordException) return
        reportError(context, e)
    }

    fun signIn(login: String, password: String, onFail: (String) -> Unit) {
        val normalizedLogin = login
            .trim()
            .contextual(case = { !it.endsWith("@mai.education") }) { "$this@mai.education" }
        val normalizedPassword = password
            .trim()

        if (normalizedLogin.isEmpty() || normalizedPassword.isEmpty()) {
            onFail("Введите логин и пароль")
            return
        }

        viewModelScope.launch {
            try {
                MyMaiApi.authorize(normalizedLogin, normalizedPassword)
                // The same password that has just worked, otherwise the next refresh fails with trailing spaces
                accountRepository.updateCredentials(normalizedLogin, normalizedPassword)
                refresh()
            } catch (e: CancellationException) {
                throw e
            } catch (e: InvalidLoginOrPasswordException) {
                onFail("Неверный логин или пароль")
            } catch (e: Exception) {
                if (e.isNoConnectionError()) {
                    onFail("Нет подключения к интернету")
                    return@launch
                }

                reportError("MyMai sign in", e)
                onFail(
                    when {
                        e is AuthenticationServerException -> "Ошибка сервера (Попробуйте отключить VPN)"
                        e is IOException && e.message == "Connection reset by peer" -> "Соединение сброшено (Попробуйте отключить VPN)"
                        e is IOException -> e.toString()
                        else -> "Неизвестная ошибка: ${e.message}"
                    }
                )
            }
        }
    }

    fun signOut() {
        accountLoading?.cancel()
        marksReloading?.cancel()
        accountRepository.clearCredentials()
        emit(initialState.copy(loggedIn = accountRepository.hasCredentials()))
    }

    fun setSelectedStudent(student: Student) {
        ApplicationSettings.setSelectedStudentId(student.id)
        emit(stateValue.copy(student = Loadable.actual(student)))
        // refresh()
    }

    class Factory: ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
            return AccountViewModel(

            ) as T
        }
    }
}