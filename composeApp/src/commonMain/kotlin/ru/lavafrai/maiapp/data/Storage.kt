package ru.lavafrai.maiapp.data

interface Storage {
    fun hasKey(key: String): Boolean
    fun remove(key: String)
    fun get(key: String): String?
    fun set(key: String, value: String?)
}
