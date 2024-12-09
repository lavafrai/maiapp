package ru.lavafrai.maiapp.viewmodels.main

import ru.lavafrai.maiapp.data.Loadable
import ru.lavafrai.maiapp.models.exler.ExlerTeacher
import ru.lavafrai.maiapp.models.schedule.LessonType
import ru.lavafrai.maiapp.models.schedule.Schedule
import ru.lavafrai.maiapp.models.time.DateRange

data class MainPageState(
    val schedule: Loadable<Schedule>,
    val selectedWeek: DateRange,
    val workTypeSelected: List<LessonType>,
    val exlerTeachers: Loadable<List<ExlerTeacher>>,
)