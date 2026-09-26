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
import maiapp.composeapp.generated.resources.Res
import maiapp.composeapp.generated.resources.connection_reset_try_without_vpn
import maiapp.composeapp.generated.resources.enter_login_and_password
import maiapp.composeapp.generated.resources.invalid_login_or_password
import maiapp.composeapp.generated.resources.no_internet_connection
import maiapp.composeapp.generated.resources.server_error_try_without_vpn
import maiapp.composeapp.generated.resources.unknown_error
import org.jetbrains.compose.resources.getString
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
        val trimmedLogin = login.trim()
        val normalizedLogin = trimmedLogin
            .contextual(case = { !it.endsWith("@mai.education") }) { "$this@mai.education" }
        val normalizedPassword = password
            .trim()

        viewModelScope.launch {
            // Not normalizedLogin: an empty login becomes "@mai.education" there
            if (trimmedLogin.isEmpty() || normalizedPassword.isEmpty()) {
                onFail(getString(Res.string.enter_login_and_password))
                return@launch
            }

            try {
                MyMaiApi.authorize(normalizedLogin, normalizedPassword)
                // The same password that has just worked, otherwise the next refresh fails with trailing spaces
                accountRepository.updateCredentials(normalizedLogin, normalizedPassword)
                refresh()
            } catch (e: CancellationException) {
                throw e
            } catch (e: InvalidLoginOrPasswordException) {
                onFail(getString(Res.string.invalid_login_or_password))
            } catch (e: Exception) {
                if (e.isNoConnectionError()) {
                    onFail(getString(Res.string.no_internet_connection))
                    return@launch
                }

                reportError("MyMai sign in", e)
                onFail(
                    when {
                        e is AuthenticationServerException -> getString(Res.string.server_error_try_without_vpn)
                        e is IOException && e.message == "Connection reset by peer" -> getString(Res.string.connection_reset_try_without_vpn)
                        e is IOException -> e.toString()
                        else -> getString(Res.string.unknown_error, e.message.orEmpty())
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