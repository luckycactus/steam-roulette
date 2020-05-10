package ru.luckycactus.steamroulette.di.common

import ru.luckycactus.steamroulette.data.SyncGamesPeriodicJobWorkManagerImpl
import ru.luckycactus.steamroulette.data.core.CacheHelper
import ru.luckycactus.steamroulette.presentation.common.App
import ru.luckycactus.steamroulette.presentation.features.main.MainActivityComponent

interface BaseAppComponent {

    fun mainActivityComponentFactory(): MainActivityComponent.Factory

    fun inject(app: App)

    fun inject(app: SyncGamesPeriodicJobWorkManagerImpl.SyncGamesPeriodicWorker)

    val cacheHelper: CacheHelper
}