package ru.lavafrai.maiapp.network.mymai.exceptions

import ru.lavafrai.maiapp.models.exceptions.MaiAppException

/** my.mai.ru or esia.mai.ru answered not as expected */
class AuthenticationServerException(message: String? = null) : MaiAppException(message) {
    override fun getReadableDescription(): String = "Ошибка сервера личного кабинета МАИ"
}
