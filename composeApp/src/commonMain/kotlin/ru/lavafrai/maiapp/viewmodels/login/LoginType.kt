package ru.lavafrai.maiapp.viewmodels.login

import kotlinx.serialization.Serializable

@Serializable
enum class LoginType {
    STUDENT,
    TEACHER,
}