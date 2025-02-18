package ru.lavafrai.maiapp.viewmodels.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import io.ktor.util.network.*
import kotlinx.coroutines.launch
import kotlinx.io.IOException
import ru.lavafrai.maiapp.data.Loadable
import ru.lavafrai.maiapp.data.repositories.AccountRepository
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
        person = Loadable.loading(),
    )
) {
    init {
        refresh()
    }

    fun refresh() {
        emit(AccountViewState(
            loggedIn = accountRepository.hasCredentials(),
            person = Loadable.loading(),
        ))

        val credentials = accountRepository.getCredentials() ?: return

        launchCatching(onError = { emit(stateValue.copy(person = Loadable.error(it as Exception))) }) {
            val session = MyMaiApi.authorize(credentials.login, credentials.password)

            val person = session.person()
            emit(stateValue.copy(person = Loadable.actual(person)))
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
            }
        }
    }

    fun signOut() {
        accountRepository.clearCredentials()
        refresh()
    }

    class Factory: ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
            return AccountViewModel(

            ) as T
        }
    }
}