package ru.luckycactus.steamroulette.presentation.common

import androidx.multidex.MultiDexApplication
import ru.luckycactus.steamroulette.di.common.BaseAppComponent
import ru.luckycactus.steamroulette.di.common.DaggerReleaseBaseAppComponent
import ru.luckycactus.steamroulette.di.core.ComponentOwner
import ru.luckycactus.steamroulette.di.core.InjectionManager
import ru.luckycactus.steamroulette.domain.app.SystemLanguageSynchronizer
import javax.inject.Inject

open class App : MultiDexApplication(),
    ComponentOwner<BaseAppComponent> {
    @Inject
    lateinit var systemLanguageSynchronizer: SystemLanguageSynchronizer

    override fun onCreate() {
        super.onCreate()
        InjectionManager.init(this)
        InjectionManager.bindComponent(this).inject(this)
        instance = this

        systemLanguageSynchronizer.start()
    }

    override fun createComponent(): BaseAppComponent =
        DaggerReleaseBaseAppComponent.builder()
            .application(this)
            .build()

    companion object {
        fun getInstance(): App = instance
        private lateinit var instance: App
    }
}