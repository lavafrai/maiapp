package ru.lavafrai.maiapp.navigation.pages

import kotlinx.serialization.Serializable
import ru.lavafrai.maiapp.viewmodels.login.LoginTarget
import ru.lavafrai.maiapp.viewmodels.login.LoginType

@Serializable
data class LoginPage(
    val type: LoginType,
    val target: LoginTarget,
    val navigateImmediately: Boolean = true
)