package ru.luckycactus.steamroulette.presentation.common

import android.app.Application
import ru.luckycactus.steamroulette.di.common.AppComponent
import ru.luckycactus.steamroulette.di.common.ComponentOwner
import ru.luckycactus.steamroulette.di.common.DaggerAppComponent
import ru.luckycactus.steamroulette.di.common.InjectionManager

class App : Application(), ComponentOwner<AppComponent> {

    override fun onCreate() {
        super.onCreate()
        InjectionManager.init(this)
        InjectionManager.bindComponent(this).inject(this)
    }

    override fun createComponent(): AppComponent =
        DaggerAppComponent.builder()
            .application(this)
            .build()
}