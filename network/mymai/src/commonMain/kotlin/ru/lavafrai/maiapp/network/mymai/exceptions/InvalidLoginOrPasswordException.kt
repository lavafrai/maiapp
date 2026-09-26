package ru.lavafrai.maiapp.network.mymai.exceptions

import ru.lavafrai.maiapp.models.exceptions.MaiAppException

class InvalidLoginOrPasswordException : MaiAppException() {
    override fun getReadableDescription(): String = "Неверный логин или пароль. Если вы меняли пароль, выйдите и войдите снова"
}
