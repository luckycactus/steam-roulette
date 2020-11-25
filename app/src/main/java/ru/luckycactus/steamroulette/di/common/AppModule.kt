package ru.luckycactus.steamroulette.di.common

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import ru.luckycactus.steamroulette.BuildConfig
import ru.luckycactus.steamroulette.data.local.AndroidResourceManager
import ru.luckycactus.steamroulette.data.local.LanguageProviderImpl
import ru.luckycactus.steamroulette.data.repositories.app.GamesPeriodicUpdateManager
import ru.luckycactus.steamroulette.data.repositories.games.owned.datasource.GamesVerifier
import ru.luckycactus.steamroulette.data.repositories.games.owned.datasource.GamesVerifierImpl
import ru.luckycactus.steamroulette.di.AppCoScope
import ru.luckycactus.steamroulette.domain.app.GamesPeriodicUpdater
import ru.luckycactus.steamroulette.domain.common.ImageCacheCleaner
import ru.luckycactus.steamroulette.domain.common.LanguageProvider
import ru.luckycactus.steamroulette.domain.core.Clock
import ru.luckycactus.steamroulette.domain.core.ResourceManager
import ru.luckycactus.steamroulette.domain.core.SystemClock
import ru.luckycactus.steamroulette.presentation.utils.AnalyticsHelper
import ru.luckycactus.steamroulette.presentation.utils.FirebaseAnalyticsHelper
import ru.luckycactus.steamroulette.presentation.utils.glide.GlideCacheCleaner
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    abstract fun bindClock(clock: SystemClock): Clock

    @Binds
    abstract fun bindResourceManager(androidResourceManager: AndroidResourceManager): ResourceManager

    @Binds
    abstract fun bindAnalytics(firebaseAnalytics: FirebaseAnalyticsHelper): AnalyticsHelper

    @Binds
    abstract fun bindGamesPeriodicFetcherManager(job: GamesPeriodicUpdateManager): GamesPeriodicUpdater.Manager

    @Binds
    abstract fun bindGameCoverCacheCleaner(glideCacheCleaner: GlideCacheCleaner): ImageCacheCleaner

    @Binds
    abstract fun bindLanguageProvider(languageProviderImpl: LanguageProviderImpl): LanguageProvider

    companion object {
        @Singleton
        @Provides
        @AppCoScope
        fun provideApplicationCoroutineScope(): CoroutineScope =
            CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

        @Provides
        @Reusable
        fun provideGamesVerifier(): GamesVerifier.Factory {
            return GamesVerifierImpl.Factory(BuildConfig.DEBUG)
        }
    }
}