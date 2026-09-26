package ru.lavafrai.maiapp.network.mymai.exceptions

import ru.lavafrai.maiapp.models.exceptions.MaiAppException

class InvalidLoginOrPasswordException : MaiAppException() {
    // The app shows a translated text instead, see readableDescription() there
    override fun getReadableDescription(): String = "Invalid login or password"
}
