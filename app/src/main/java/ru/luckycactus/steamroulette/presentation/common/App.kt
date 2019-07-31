package ru.luckycactus.steamroulette.presentation.common

import android.app.Application
import ru.luckycactus.steamroulette.di.DI

//todo prefetch
class App: Application() {

    override fun onCreate() {
        super.onCreate()
        DI.init(this)
    }
}