package ru.lavafrai.maiapp.viewmodels.account

import ru.lavafrai.maiapp.data.Loadable
import ru.lavafrai.maiapp.models.account.Mark
import ru.lavafrai.maiapp.models.account.Student
import ru.lavafrai.maiapp.models.account.StudentInfo
import ru.lavafrai.maiapp.models.account.StudentMarks


data class AccountViewState(
    val loggedIn: Boolean,
    val studentInfo: Loadable<StudentInfo>,
    val student: Loadable<Student?>,
    val marks: Loadable<StudentMarks>,
)