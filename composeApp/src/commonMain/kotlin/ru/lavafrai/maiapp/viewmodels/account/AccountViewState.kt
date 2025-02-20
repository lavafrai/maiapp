package ru.lavafrai.maiapp.viewmodels.account

import ru.lavafrai.maiapp.data.Loadable
import ru.lavafrai.maiapp.models.account.StudentInfo


data class AccountViewState(
    val loggedIn: Boolean,
    val studentInfo: Loadable<StudentInfo>,
)