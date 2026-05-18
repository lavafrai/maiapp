package ru.lavafrai.maiapp.viewmodels.teacherReview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import ru.lavafrai.maiapp.BuildConfig.API_BASE_URL
import ru.lavafrai.maiapp.data.Loadable
import ru.lavafrai.maiapp.data.repositories.ExlerRepository
import ru.lavafrai.maiapp.models.schedule.TeacherUid
import ru.lavafrai.maiapp.viewmodels.MaiAppViewModel
import kotlin.reflect.KClass

class TeacherReviewViewModel(
    val teacherId: String,
    val teacherUid: TeacherUid,
): MaiAppViewModel<TeacherReviewsState>(
    TeacherReviewsState(
        teacherInfo = Loadable.loading()
    )
) {
    val exlerRepository = ExlerRepository(
        httpClient = httpClient,
        baseUrl = API_BASE_URL
    )

    init {
        startLoading()
    }

    fun startLoading() {
        viewModelScope.launch (dispatchers.IO) {
            emit(initialState)

            val teacherReviewsHandler = CoroutineExceptionHandler { _, e ->
                e.printStackTrace()
                emit(stateValue.copy(teacherInfo = stateValue.teacherInfo.copy(error = e as Exception)))
            }

            supervisorScope {
                launch(teacherReviewsHandler) {
                    val teacherInfo = exlerRepository.getTeacherInfo(teacherId)
                    emit(stateValue.copy(teacherInfo = Loadable.actual(teacherInfo)))
                }
            }
        }
    }

    class Factory(
        private val teacherId: String,
        private val teacherUid: TeacherUid,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
            return TeacherReviewViewModel(teacherId, teacherUid) as T
        }
    }
}