package ru.luckycactus.steamroulette.presentation

import android.app.Application
import ru.luckycactus.steamroulette.di.DI

class App: Application() {

    override fun onCreate() {
        super.onCreate()
        DI.init(this)
    }
}