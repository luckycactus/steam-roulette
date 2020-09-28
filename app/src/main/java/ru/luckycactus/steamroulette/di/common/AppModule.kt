package ru.luckycactus.steamroulette.di.common

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import ru.luckycactus.steamroulette.BuildConfig
import ru.luckycactus.steamroulette.data.local.AndroidResourceManager
import ru.luckycactus.steamroulette.data.local.LanguageProviderImpl
import ru.luckycactus.steamroulette.data.repositories.about.AboutRepositoryImpl
import ru.luckycactus.steamroulette.data.repositories.about.datasource.AboutDataSource
import ru.luckycactus.steamroulette.data.repositories.about.datasource.LocalAboutDataSource
import ru.luckycactus.steamroulette.data.repositories.app.AppRepositoryImpl
import ru.luckycactus.steamroulette.data.repositories.app.GamesPeriodicFetcherManager
import ru.luckycactus.steamroulette.data.repositories.games.details.GameDetailsRepositoryImpl
import ru.luckycactus.steamroulette.data.repositories.games.owned.GamesRepositoryImpl
import ru.luckycactus.steamroulette.data.repositories.games.owned.datasource.*
import ru.luckycactus.steamroulette.data.repositories.games.details.datasource.GameStoreDataSource
import ru.luckycactus.steamroulette.data.repositories.games.details.datasource.RemoteGameStoreDataSource
import ru.luckycactus.steamroulette.data.repositories.login.LoginRepositoryImpl
import ru.luckycactus.steamroulette.data.repositories.login.datasource.LoginDataSource
import ru.luckycactus.steamroulette.data.repositories.login.datasource.RemoteLoginDataSource
import ru.luckycactus.steamroulette.data.repositories.review.AppReviewRepositoryImpl
import ru.luckycactus.steamroulette.data.repositories.user.UserRepositoryImpl
import ru.luckycactus.steamroulette.data.repositories.user.UserSessionRepositoryImpl
import ru.luckycactus.steamroulette.data.repositories.user.datasource.LocalUserDataSource
import ru.luckycactus.steamroulette.data.repositories.user.datasource.RemoteUserDataSource
import ru.luckycactus.steamroulette.data.repositories.user.datasource.UserDataSource
import ru.luckycactus.steamroulette.data.repositories.user_settings.UserSettingsRepositoryImpl
import ru.luckycactus.steamroulette.di.AppCoScope
import ru.luckycactus.steamroulette.domain.about.AboutRepository
import ru.luckycactus.steamroulette.domain.app.AppRepository
import ru.luckycactus.steamroulette.domain.app.GamesPeriodicFetcher
import ru.luckycactus.steamroulette.domain.common.ImageCacheCleaner
import ru.luckycactus.steamroulette.domain.common.LanguageProvider
import ru.luckycactus.steamroulette.domain.core.Clock
import ru.luckycactus.steamroulette.domain.core.ResourceManager
import ru.luckycactus.steamroulette.domain.core.SystemClock
import ru.luckycactus.steamroulette.domain.games.GameDetailsRepository
import ru.luckycactus.steamroulette.domain.games.GamesRepository
import ru.luckycactus.steamroulette.domain.login.LoginRepository
import ru.luckycactus.steamroulette.domain.review.AppReviewRepository
import ru.luckycactus.steamroulette.domain.user.UserRepository
import ru.luckycactus.steamroulette.domain.user.UserSessionRepository
import ru.luckycactus.steamroulette.domain.user_settings.UserSettingsRepository
import ru.luckycactus.steamroulette.presentation.features.roulette.GlideCacheCleaner
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
abstract class AppModule {

    @Binds
    abstract fun bindClock(clock: SystemClock): Clock

    @Binds
    abstract fun bindResourceManager(androidResourceManager: AndroidResourceManager): ResourceManager

    @Binds
    abstract fun bindGamesPeriodicFetcherManager(job: GamesPeriodicFetcherManager): GamesPeriodicFetcher.Manager

    @Binds
    abstract fun bindAppSettingsRepository(appRepositoryImpl: AppRepositoryImpl): AppRepository

    @Binds
    abstract fun bindUserRepository(userRepository: UserRepositoryImpl): UserRepository

    @Binds
    abstract fun bindUserSessionRepository(userSessionRepository: UserSessionRepositoryImpl): UserSessionRepository

    @Binds
    abstract fun bindUserSettingsRepository(userSettingsRepository: UserSettingsRepositoryImpl): UserSettingsRepository

    @Binds
    abstract fun bindLoginRepository(loginRepository: LoginRepositoryImpl): LoginRepository

    @Binds
    abstract fun bindRemoteUserDataSource(dataSource: RemoteUserDataSource): UserDataSource.Remote

    @Binds
    abstract fun bindGamesRepository(gamesRepository: GamesRepositoryImpl): GamesRepository

    @Binds
    abstract fun bindGameDetailsRepository(gameDetailsRepositoryImpl: GameDetailsRepositoryImpl): GameDetailsRepository

    @Binds
    abstract fun bindAboutRepository(aboutRepositoryImpl: AboutRepositoryImpl): AboutRepository

    @Binds
    abstract fun bindAppReviewRepository(appReviewRepositoryImpl: AppReviewRepositoryImpl): AppReviewRepository

    @Binds
    abstract fun bindGameCoverCacheCleaner(glideCacheCleaner: GlideCacheCleaner): ImageCacheCleaner

    @Binds
    abstract fun bindRemoteGamesDataSource(remoteGamesDataSource: RemoteGamesDataSource): GamesDataSource.Remote

    @Binds
    abstract fun bindLocalGamesDataSource(localGamesDataSource: LocalGamesDataSource): GamesDataSource.Local

    @Binds
    abstract fun bindLocalUserDataSource(localUserDataSource: LocalUserDataSource): UserDataSource.Local

    @Binds
    abstract fun bindLoginDataSource(loginDataSource: RemoteLoginDataSource): LoginDataSource

    @Binds
    abstract fun bindLanguageProvider(languageProviderImpl: LanguageProviderImpl): LanguageProvider

    @Binds
    abstract fun bindAboutDataSource(localAboutDataSource: LocalAboutDataSource): AboutDataSource

    @Binds
    abstract fun bindRemoteGameDetailsDataSource(remoteGameDetailsDataSource: RemoteGameStoreDataSource): GameStoreDataSource.Remote

    companion object {

        @Singleton
        @Provides
        @AppCoScope
        fun provideApplicationCoroutineScope() =
            CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

        @Provides
        @Reusable
        fun provideGamesVerifier(): GamesVerifier.Factory {
            return GamesVerifierImpl.Factory(BuildConfig.DEBUG)
        }
    }
}