@file:OptIn(ExperimentalComposeUiApi::class, ExperimentalComposeUiApi::class)

package ru.lavafrai.maiapp.fragments.account

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.platform.LocalAutofill
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import compose.icons.FeatherIcons
import compose.icons.feathericons.Eye
import compose.icons.feathericons.EyeOff
import maiapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import ru.lavafrai.maiapp.fragments.AnimatedIcon
import ru.lavafrai.maiapp.fragments.PageColumn
import ru.lavafrai.maiapp.platform.CredentialField
import ru.lavafrai.maiapp.platform.credentialImeOptions
import ru.lavafrai.maiapp.utils.autofill
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
    // Not saveable: the sign in itself doesn't survive process death, so a restored true would block the form forever
    var loading by remember { mutableStateOf(false) }
    var error: String? by remember { mutableStateOf(null) }
    val focusManager = LocalFocusManager.current
    var login by remember { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordHidden by remember { mutableStateOf(true) }
    val autofillManager = LocalAutofill.current

    Spacer(Modifier.height(16.dp))
    Text(stringResource(Res.string.profile), style = MaterialTheme.typography.headlineMedium)
    OutlinedTextField(
        value = login,
        onValueChange = { login = it ; error = null },
        label = { Text(stringResource(Res.string.login)) },
        shape = MaterialTheme.shapes.large,
        singleLine = true,
        enabled = !loading,
        modifier = Modifier
            .fillMaxWidth()
            .autofill(
                autofillTypes = listOf(
                    AutofillType.Username,
                    AutofillType.EmailAddress
                ),
                onFill = { login = it },
            ),
            //.semantics { contentType = ContentType.Username },
        isError = error != null,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            platformImeOptions = credentialImeOptions(CredentialField.Username),
        ),
    )
    OutlinedTextField(
        value = password,
        onValueChange = { password = it ; error = null },
        label = { Text(stringResource(Res.string.password)) },
        shape = MaterialTheme.shapes.large,
        singleLine = true,
        enabled = !loading,
        modifier = Modifier
            .fillMaxWidth()
            .autofill(
                autofillTypes = listOf(
                    AutofillType.Password
                ),
                onFill = { password = it },
            ),
            //.semantics { contentType = ContentType.Password },
        isError = error != null,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            platformImeOptions = credentialImeOptions(CredentialField.Password),
        ),
        visualTransformation = if (passwordHidden) PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon = {
            IconButton(onClick = { passwordHidden = !passwordHidden }) {
                AnimatedIcon(
                    iconPainter = FeatherIcons.Eye,
                    enabledIconPainter = FeatherIcons.EyeOff,
                    enabled = passwordHidden,
                    contentDescription = "Show password",
                )
            }
        },
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
        shapes = ButtonDefaults.shapes(),
    ) {
        Text(stringResource(Res.string.sign_in))
    }

    Spacer(Modifier.height(24.dp))
    val loginInfo = stringResource(Res.string.account_login_info).lines()
    Text(
        loginInfo.first(),
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
    )
    loginInfo.getOrNull(1)?.let {
        Text(
            it,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
        )
    }

    Spacer(Modifier.windowInsetsPadding(WindowInsets.navigationBars))
}
