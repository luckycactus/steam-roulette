package ru.luckycactus.steamroulette

import com.facebook.stetho.Stetho
import ru.luckycactus.steamroulette.presentation.common.App

class DebugApp: App() {

    override fun onCreate() {
        super.onCreate()
        Stetho.initializeWithDefaults(this)
    }
}