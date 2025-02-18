package ru.lavafrai.maiapp.fragments.account

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import maiapp.composeapp.generated.resources.*
import maiapp.composeapp.generated.resources.Res
import maiapp.composeapp.generated.resources.login
import maiapp.composeapp.generated.resources.password
import maiapp.composeapp.generated.resources.profile
import org.jetbrains.compose.resources.stringResource
import ru.lavafrai.maiapp.fragments.PageColumn
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import ru.lavafrai.maiapp.viewmodels.account.AccountViewModel

@Composable
fun AccountPageLogin(
    viewModel: AccountViewModel,
) = PageColumn (
    modifier = Modifier
        .fillMaxWidth(0.8f),

    verticalArrangement = Arrangement.spacedBy(12.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    paddings = true,
) {
    var loading by rememberSaveable { mutableStateOf(false) }
    var error: String? by remember { mutableStateOf(null) }
    val focusManager = LocalFocusManager.current
    var login by remember { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    Spacer(Modifier.height(16.dp))
    Text(stringResource(Res.string.profile), style = MaterialTheme.typography.headlineMedium)
    OutlinedTextField(
        value = login,
        onValueChange = { login = it ; error = null },
        label = { Text(stringResource(Res.string.login)) },
        shape = MaterialTheme.shapes.large,
        singleLine = true,
        enabled = !loading,
        modifier = Modifier.fillMaxWidth(),
        isError = error != null,
    )
    OutlinedTextField(
        value = password,
        onValueChange = { password = it ; error = null },
        label = { Text(stringResource(Res.string.password)) },
        shape = MaterialTheme.shapes.large,
        singleLine = true,
        enabled = !loading,
        modifier = Modifier.fillMaxWidth(),
        isError = error != null,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
    )
    AnimatedVisibility(visible = error != null) {
        Text(
            error ?: "",
            color = MaterialTheme.colorScheme.error,
        )
    }
    Button(
        modifier = Modifier.align(Alignment.End),
        onClick = {
            focusManager.clearFocus()
            loading = true
            error = null
            viewModel.signIn(login, password) { result ->
                loading = false
                error = result
            }
        },
        enabled = !loading,
    ) {
        Text(stringResource(Res.string.sign_in))
    }

    Spacer(Modifier.height(24.dp))
    Text(
        stringResource(Res.string.account_login_info).split("\n")[0],
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
    )
    Text(
        stringResource(Res.string.account_login_info).split("\n")[1],
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
    )

    Spacer(Modifier.windowInsetsPadding(WindowInsets.navigationBars))
}