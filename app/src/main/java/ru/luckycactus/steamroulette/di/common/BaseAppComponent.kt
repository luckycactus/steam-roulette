package ru.luckycactus.steamroulette.di.common

import ru.luckycactus.steamroulette.data.repositories.app.GamesPeriodicFetcherManager
import ru.luckycactus.steamroulette.data.core.CacheHelper
import ru.luckycactus.steamroulette.presentation.common.App
import ru.luckycactus.steamroulette.presentation.features.main.MainActivityComponent

interface BaseAppComponent {

    fun mainActivityComponentFactory(): MainActivityComponent.Factory

    fun inject(app: App)

    fun inject(app: GamesPeriodicFetcherManager.Worker)

    val cacheHelper: CacheHelper
}