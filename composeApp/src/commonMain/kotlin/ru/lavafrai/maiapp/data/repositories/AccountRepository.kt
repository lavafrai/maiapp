package ru.lavafrai.maiapp.data.repositories

class AccountRepository: BaseRepository() {
    fun hasCredentials(): Boolean {
        return false
    }
}

