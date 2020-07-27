package ru.luckycactus.steamroulette

import com.facebook.stetho.Stetho
import dagger.hilt.android.HiltAndroidApp
import ru.luckycactus.steamroulette.presentation.common.App

@HiltAndroidApp
class DebugApp: App() {

    override fun onCreate() {
        super.onCreate()
        Stetho.initializeWithDefaults(this)
    }
}