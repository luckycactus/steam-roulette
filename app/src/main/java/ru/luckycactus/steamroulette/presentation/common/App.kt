package ru.luckycactus.steamroulette.presentation.common

import android.app.Application
import ru.luckycactus.steamroulette.di.common.AppComponent
import ru.luckycactus.steamroulette.di.common.DaggerReleaseAppComponent
import ru.luckycactus.steamroulette.di.core.ComponentOwner
import ru.luckycactus.steamroulette.di.core.InjectionManager

open class App : Application(),
    ComponentOwner<AppComponent> {

    companion object {
        fun getInstance(): App = instance
        private lateinit var instance: App
    }

    override fun onCreate() {
        super.onCreate()
        InjectionManager.init(this)
        InjectionManager.bindComponent(this).inject(this)
        instance = this
    }

    override fun createComponent(): AppComponent =
        DaggerReleaseAppComponent.builder()
            .application(this)
            .build()
}