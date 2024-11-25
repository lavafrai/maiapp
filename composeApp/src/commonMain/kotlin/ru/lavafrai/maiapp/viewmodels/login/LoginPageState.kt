package ru.lavafrai.maiapp.viewmodels.login

import ru.lavafrai.maiapp.data.Loadable
import ru.lavafrai.maiapp.models.Nameable
import ru.lavafrai.maiapp.models.group.Group
import ru.lavafrai.maiapp.models.schedule.TeacherId
import ru.lavafrai.maiapp.navigation.Pages

data class LoginPageState(
    val groups: Loadable<List<Group>>,
    val teachers: Loadable<List<TeacherId>>,
    val filteredData: List<Nameable>,
    private val loginData: Pages.Login,
    val selected: Nameable?,
) {
    val neededLoadable = when (loginData.type) {
        LoginType.STUDENT -> groups
        LoginType.TEACHER -> teachers
    }
}