package ru.lavafrai.maiapp.viewmodels.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.lavafrai.maiapp.data.repositories.AccountRepository
import ru.lavafrai.maiapp.viewmodels.MaiAppViewModel
import kotlin.reflect.KClass

class AccountViewModel(
    private val accountRepository: AccountRepository = AccountRepository(),
): MaiAppViewModel<AccountViewState>(
    initialState = AccountViewState(
        loggedIn = accountRepository.hasCredentials()
    )
) {
    init {

    }

    fun signIn(login: String, password: String, onFail: (String) -> Unit) {
        viewModelScope.launch {
            delay(1000)
            onFail("Error: Not implemented")
        }
    }

    class Factory: ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
            return AccountViewModel(

            ) as T
        }
    }
}