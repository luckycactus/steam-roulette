package ru.luckycactus.steamroulette.di.common

import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import ru.luckycactus.steamroulette.BuildConfig
import ru.luckycactus.steamroulette.data.local.AndroidResourceManager
import ru.luckycactus.steamroulette.data.local.LanguageProviderImpl
import ru.luckycactus.steamroulette.data.repositories.app.GamesPeriodicUpdateScheduler
import ru.luckycactus.steamroulette.data.repositories.games.owned.datasource.GamesValidator
import ru.luckycactus.steamroulette.data.repositories.games.owned.datasource.GamesValidatorImpl
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
    abstract fun bindGamesPeriodicFetcherManager(job: GamesPeriodicUpdateScheduler): GamesPeriodicUpdater.Scheduler

    @Binds
    abstract fun bindGameCoverCacheCleaner(glideCacheCleaner: GlideCacheCleaner): ImageCacheCleaner

    @Binds
    abstract fun bindLanguageProvider(languageProviderImpl: LanguageProviderImpl): LanguageProvider

    companion object {
        @Provides
        @AppCoScope
        fun provideApplicationCoroutineScope(): CoroutineScope =
            ProcessLifecycleOwner.get().lifecycleScope

        @Provides
        @Reusable
        fun provideGamesVerifier(): GamesValidator.Factory {
            return GamesValidatorImpl.Factory(BuildConfig.DEBUG)
        }
    }
}