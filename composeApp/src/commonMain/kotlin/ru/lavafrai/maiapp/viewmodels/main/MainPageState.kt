package ru.lavafrai.maiapp.viewmodels.main

import ru.lavafrai.maiapp.data.Loadable
import ru.lavafrai.maiapp.models.schedule.Schedule

data class MainPageState(
    val schedule: Loadable<Schedule>,
)