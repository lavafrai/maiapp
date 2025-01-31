package ru.lavafrai.maiapp.viewmodels.dedicatedSchedule

import ru.lavafrai.maiapp.data.Loadable
import ru.lavafrai.maiapp.models.exler.ExlerTeacher
import ru.lavafrai.maiapp.models.schedule.Schedule

data class DedicatedScheduleViewState(
    val schedule: Loadable<Schedule>,
    val exlerTeachers: Loadable<List<ExlerTeacher>>,
)