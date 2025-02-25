package ru.lavafrai.maiapp.viewmodels.login

import ru.lavafrai.maiapp.data.Loadable
import ru.lavafrai.maiapp.models.Nameable
import ru.lavafrai.maiapp.models.group.Group
import ru.lavafrai.maiapp.models.schedule.TeacherId
import ru.lavafrai.maiapp.navigation.pages.LoginPage

data class LoginPageState(
    val groups: Loadable<List<Group>>,
    val teachers: Loadable<List<TeacherId>>,
    val exler: Loadable<List<Nameable>>,
    val filteredData: List<Nameable>,
    private val loginData: LoginPage,
    val selected: Nameable?,
) {
    val neededLoadable = when (loginData.type) {
        LoginType.STUDENT -> groups
        LoginType.TEACHER -> teachers
        LoginType.EXLER -> exler
    }
}