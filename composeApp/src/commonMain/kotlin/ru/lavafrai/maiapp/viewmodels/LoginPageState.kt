package ru.lavafrai.maiapp.viewmodels

import ru.lavafrai.maiapp.data.Loadable
import ru.lavafrai.maiapp.models.group.Group
import ru.lavafrai.maiapp.models.schedule.TeacherId

data class LoginPageState(
    val groups: Loadable<List<Group>>,
    val teachers: Loadable<List<TeacherId>>,
)