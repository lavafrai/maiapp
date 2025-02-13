package ru.lavafrai.maiapp.fragments.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ru.lavafrai.maiapp.viewmodels.account.AccountViewModel
import soup.compose.material.motion.MaterialSharedAxisY

@Composable
fun AccountPage(
    viewModel: AccountViewModel,
    modifier: Modifier = Modifier,
) {
    val viewState by viewModel.state.collectAsState()

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize(),
    ) {
        MaterialSharedAxisY(
            targetState = if (viewState.loggedIn) AccountPageState.Logged else AccountPageState.Unlogged,
            forward = true,
        ) {
            when (viewState.loggedIn) {
                true -> Text("Logged in")
                false -> AccountPageLogin(
                    viewModel = viewModel,
                )
            }
        }
    }
}

enum class AccountPageState {
    Unlogged,
    Logged,
}