package ru.luckycactus.steamroulette.presentation.common

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.util.DebugLogger
import com.squareup.moshi.Moshi
import kotlinx.coroutines.runBlocking
import ru.luckycactus.steamroulette.BuildConfig
import ru.luckycactus.steamroulette.data.core.SharedPreferencesConfig
import ru.luckycactus.steamroulette.domain.app.GamesPeriodicUpdater
import ru.luckycactus.steamroulette.domain.app.MigrateAppUseCase
import ru.luckycactus.steamroulette.domain.app.SystemLanguageSynchronizer
import ru.luckycactus.steamroulette.domain.core.usecase.invoke
import ru.luckycactus.steamroulette.domain.review.AppReviewExceptionsHandler
import ru.luckycactus.steamroulette.domain.review.AppReviewManager
import timber.log.Timber
import javax.inject.Inject

open class App : Application(), Configuration.Provider, ImageLoaderFactory {

    @Inject
    lateinit var systemLanguageSynchronizer: SystemLanguageSynchronizer

    @Inject
    lateinit var gamesPeriodicUpdater: GamesPeriodicUpdater

    @Inject
    lateinit var appReviewManager: AppReviewManager

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var migrateApp: MigrateAppUseCase

    @Inject
    lateinit var moshi: Moshi

    override fun onCreate() {
        super.onCreate()
        instance = this

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        if (!appReviewManager.isRated()) {
            Thread.setDefaultUncaughtExceptionHandler(AppReviewExceptionsHandler(appReviewManager))
        }

        SharedPreferencesConfig.moshi = moshi

        systemLanguageSynchronizer.start()
        gamesPeriodicUpdater.start()

        //todo
        runBlocking {
            migrateApp()
        }
    }

    companion object {
        fun getInstance(): App = instance
        private lateinit var instance: App
    }

    override fun getWorkManagerConfiguration() = Configuration.Builder()
        .setWorkerFactory(workerFactory)
        .build()

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .apply {
                if (BuildConfig.DEBUG) {
                    logger(DebugLogger())
                }
            }
            .build()
    }
}