package ru.lavafrai.maiapp.navigation

import kotlinx.serialization.Serializable
import ru.lavafrai.maiapp.navigation.rootPages.login.LoginTarget
import ru.lavafrai.maiapp.navigation.rootPages.login.LoginType

object Pages {
    @Serializable
    object Main

    @Serializable
    object Greeting

    @Serializable
    data class Login(val type: LoginType, val target: LoginTarget)
}