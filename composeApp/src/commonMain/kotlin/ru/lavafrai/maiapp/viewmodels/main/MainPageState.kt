package ru.lavafrai.maiapp.viewmodels.main

import ru.lavafrai.maiapp.data.Loadable
import ru.lavafrai.maiapp.models.exler.ExlerTeacher
import ru.lavafrai.maiapp.models.maidata.MaiDataItem
import ru.lavafrai.maiapp.models.schedule.LessonType
import ru.lavafrai.maiapp.models.schedule.Schedule
import ru.lavafrai.maiapp.models.time.DateRange
import ru.lavafrai.maiapp.rootPages.main.MainNavigationPageId

data class MainPageState(
    val page: MainNavigationPageId,
    val schedule: Loadable<Schedule>,
    val maidata: Loadable<List<MaiDataItem>>,
    val selectedWeek: DateRange,
    val workTypeSelected: List<LessonType>,
    val exlerTeachers: Loadable<List<ExlerTeacher>>,
)