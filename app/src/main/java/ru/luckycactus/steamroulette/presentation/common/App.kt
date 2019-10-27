package ru.luckycactus.steamroulette.presentation.common

import android.app.Application
import ru.luckycactus.steamroulette.di.DI

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        DI.init(this)
    }

    companion object {
        private lateinit var instance: App
        fun getInstance() = instance
    }
}