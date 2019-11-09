package ru.luckycactus.steamroulette.presentation.common

import android.app.Application
import ru.luckycactus.steamroulette.di.common.*

open class App : Application(), ComponentOwner<AppComponent> {

    override fun onCreate() {
        super.onCreate()
        InjectionManager.init(this)
        InjectionManager.bindComponent(this).inject(this)
    }

    override fun createComponent(): AppComponent =
        DaggerReleaseAppComponent.builder()
            .application(this)
            .build()
}