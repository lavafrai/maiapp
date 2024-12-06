package ru.lavafrai.maiapp

class AndroidApplication : android.app.Application() {
    override fun onCreate() {
        instance = this
        super.onCreate()
    }

    companion object {
        private lateinit var instance: AndroidApplication
        fun instance() = instance
    }
}