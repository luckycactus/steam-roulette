package ru.luckycactus.steamroulette.presentation.common

import android.app.Application
import ru.luckycactus.steamroulette.di.common.AppComponent
import ru.luckycactus.steamroulette.di.common.DaggerAppComponent

class App : Application() {

    private lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        instance = this
        appComponent = DaggerAppComponent.builder()
            .application(this)
            .build()
    }

    fun appComponent() = appComponent

    companion object {
        private lateinit var instance: App
        fun getInstance() = instance
    }
}