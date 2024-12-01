package ru.lavafrai.maiapp.viewmodels.teacherReview

import ru.lavafrai.maiapp.data.Loadable
import ru.lavafrai.maiapp.models.exler.ExlerTeacherInfo

data class TeacherReviewsState(
    val teacherInfo: Loadable<ExlerTeacherInfo>,
)