package ru.luckycactus.steamroulette.di.common

import ru.luckycactus.steamroulette.data.local.CacheHelper
import ru.luckycactus.steamroulette.presentation.common.App
import ru.luckycactus.steamroulette.presentation.features.main.MainActivityComponent

interface AppComponent {

    fun mainActivityComponentFactory(): MainActivityComponent.Factory

    fun inject(app: App)

    val cacheHelper: CacheHelper
}