package ru.lavafrai.maiapp.network.mymai.exceptions

import ru.lavafrai.maiapp.models.exceptions.MaiAppException

/** my.mai.ru or esia.mai.ru answered not as expected */
class AuthenticationServerException(message: String? = null) : MaiAppException(message) {
    // The app shows a translated text instead, see readableDescription() there
    override fun getReadableDescription(): String = "my.mai.ru server error"
}
