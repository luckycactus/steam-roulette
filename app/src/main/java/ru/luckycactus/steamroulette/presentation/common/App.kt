package ru.luckycactus.steamroulette.presentation.common

import android.app.Application
import ru.luckycactus.steamroulette.di.common.AppComponent
import ru.luckycactus.steamroulette.di.common.DaggerReleaseAppComponent
import ru.luckycactus.steamroulette.di.core.ComponentOwner
import ru.luckycactus.steamroulette.di.core.InjectionManager
import ru.luckycactus.steamroulette.domain.app.SystemLanguageSynchronizer
import javax.inject.Inject

open class App : Application(),
    ComponentOwner<AppComponent> {

    @Inject
    lateinit var systemLanguageSynchronizer: SystemLanguageSynchronizer

    override fun onCreate() {
        super.onCreate()
        InjectionManager.init(this)
        InjectionManager.bindComponent(this).inject(this)
        instance = this

        systemLanguageSynchronizer.start()
    }

    override fun createComponent(): AppComponent =
        DaggerReleaseAppComponent.builder()
            .application(this)
            .build()

    companion object {
        fun getInstance(): App = instance
        private lateinit var instance: App
    }
}