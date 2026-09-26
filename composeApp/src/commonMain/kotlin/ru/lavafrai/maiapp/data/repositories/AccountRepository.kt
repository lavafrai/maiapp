package ru.lavafrai.maiapp.data.repositories

import com.russhwolf.settings.set
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import ru.lavafrai.maiapp.models.account.Credentials
import ru.lavafrai.maiapp.utils.reportError

class AccountRepository: BaseRepository() {
    private val credentialsKey = "mymai:auth:credentials"

    fun hasCredentials(): Boolean {
        return getCredentials() != null
    }

    fun updateCredentials(login: String, password: String) {
        val creds = Credentials(login, password)
        storage[credentialsKey] = json.encodeToString(creds)
    }

    fun getCredentials(): Credentials? {
        val stored = storage.getStringOrNull(credentialsKey) ?: return null
        return try {
            json.decodeFromString<Credentials>(stored)
        } catch (e: SerializationException) {
            reportError("Stored MyMai credentials", IllegalStateException("Unreadable stored credentials"))
            clearCredentials()
            null
        }
    }

    fun clearCredentials() {
        storage.remove(credentialsKey)
    }
}

