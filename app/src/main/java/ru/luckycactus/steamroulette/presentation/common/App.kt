package ru.luckycactus.steamroulette.presentation.common

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import ru.luckycactus.steamroulette.domain.app.GamesPeriodicFetcher
import ru.luckycactus.steamroulette.domain.app.SystemLanguageSynchronizer
import javax.inject.Inject

open class App : Application(), Configuration.Provider {

    @Inject
    lateinit var systemLanguageSynchronizer: SystemLanguageSynchronizer

    @Inject
    lateinit var gamesPeriodicFetcher: GamesPeriodicFetcher

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        instance = this

        systemLanguageSynchronizer.start()
        gamesPeriodicFetcher.start()
    }

    companion object {
        fun getInstance(): App = instance
        private lateinit var instance: App
    }

    override fun getWorkManagerConfiguration() = Configuration.Builder()
        .setWorkerFactory(workerFactory)
        .build()
}