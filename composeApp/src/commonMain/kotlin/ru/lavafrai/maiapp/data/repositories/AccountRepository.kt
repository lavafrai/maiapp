package ru.lavafrai.maiapp.data.repositories

import com.russhwolf.settings.set
import kotlinx.serialization.encodeToString
import ru.lavafrai.maiapp.models.account.Credentials

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
        return storage.getStringOrNull(credentialsKey)?.let { json.decodeFromString(it) }
    }

    fun clearCredentials() {
        storage.remove(credentialsKey)
    }
}

