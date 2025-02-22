package ru.lavafrai.maiapp.viewmodels.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import io.ktor.util.network.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.io.IOException
import ru.lavafrai.maiapp.data.Loadable
import ru.lavafrai.maiapp.data.repositories.AccountRepository
import ru.lavafrai.maiapp.data.settings.ApplicationSettings
import ru.lavafrai.maiapp.data.settings.rememberSettings
import ru.lavafrai.maiapp.models.account.Student
import ru.lavafrai.maiapp.network.mymai.MyMaiApi
import ru.lavafrai.maiapp.network.mymai.exceptions.AuthenticationServerException
import ru.lavafrai.maiapp.network.mymai.exceptions.InvalidLoginOrPasswordException
import ru.lavafrai.maiapp.utils.contextual
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
    init {
        refresh()
    }

    fun refresh() {
        emit(initialState.copy(loggedIn = accountRepository.hasCredentials()))

        val credentials = accountRepository.getCredentials() ?: return

        launchCatching(onError = { emit(stateValue.copy(studentInfo = Loadable.error(it as Exception))) }) {
            val session = MyMaiApi.authorize(credentials.login, credentials.password)

            val studentInfo = session.studentInfo()
            emit(stateValue.copy(studentInfo = Loadable.actual(studentInfo)))

            ApplicationSettings.state.collect { settings ->
                val selectedStudent = studentInfo.students.firstOrNull { settings.selectedStudentId == it.id }
                    ?: studentInfo.students.firstOrNull()
                emit(stateValue.copy(student = Loadable.actual(selectedStudent)))
                selectedStudent ?: throw IllegalArgumentException("No one student found")

                launchCatching(onError = {emit(stateValue.copy(marks = Loadable.error(it as Exception)))}) {
                    val marks = session.studentMarks(selectedStudent.studentCode)
                    emit(stateValue.copy(marks = Loadable.actual(marks), student = Loadable.actual(selectedStudent)))
                }
            }
        }
    }

    fun reloadMarks(student: Student) {
        launchCatching(onError = { emit(stateValue.copy(marks = Loadable.error(it as Exception))) }) {
            emit(stateValue.copy(marks = Loadable.loading()))
            val session = MyMaiApi.authorize(accountRepository.getCredentials()!!.login, accountRepository.getCredentials()!!.password)
            val marks = session.studentMarks(student.studentCode)
            emit(stateValue.copy(marks = Loadable.actual(marks)))
        }
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
                accountRepository.updateCredentials(normalizedLogin, password)
                refresh()
            } catch (e: InvalidLoginOrPasswordException) {
                onFail("Неверный логин или пароль")
            } catch (e: UnresolvedAddressException) {
                onFail("Нет подключения к интернету")
            } catch (e: IOException) {
                onFail(
                    when (e.message) {
                        "Connection reset by peer" -> "Соединение сброшено (Попробуйте отключить VPN)"
                        else -> e.toString()
                    }
                )
                return@launch
            } catch (e: AuthenticationServerException) {
                onFail("Ошибка сервера (Попробуйте отключить VPN)")
            } catch (e: Exception) {
                onFail("Неизвестная ошибка: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun signOut() {
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