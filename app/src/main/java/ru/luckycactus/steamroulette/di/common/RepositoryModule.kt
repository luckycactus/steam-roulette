package ru.luckycactus.steamroulette.di.common

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.luckycactus.steamroulette.data.repositories.about.AboutRepositoryImpl
import ru.luckycactus.steamroulette.data.repositories.about.datasource.AboutDataSource
import ru.luckycactus.steamroulette.data.repositories.about.datasource.LocalAboutDataSource
import ru.luckycactus.steamroulette.data.repositories.app.AppRepositoryImpl
import ru.luckycactus.steamroulette.data.repositories.games.details.GameDetailsRepositoryImpl
import ru.luckycactus.steamroulette.data.repositories.games.details.datasource.GameStoreDataSource
import ru.luckycactus.steamroulette.data.repositories.games.details.datasource.RemoteGameStoreDataSource
import ru.luckycactus.steamroulette.data.repositories.games.owned.GamesRepositoryImpl
import ru.luckycactus.steamroulette.data.repositories.games.owned.datasource.GamesDataSource
import ru.luckycactus.steamroulette.data.repositories.games.owned.datasource.LocalGamesDataSource
import ru.luckycactus.steamroulette.data.repositories.games.owned.datasource.RemoteGamesDataSource
import ru.luckycactus.steamroulette.data.repositories.games.roulette.RouletteRepositoryImpl
import ru.luckycactus.steamroulette.data.repositories.games_filter.LibraryFilterRepositoryImpl
import ru.luckycactus.steamroulette.data.repositories.games_filter.RouletteFilterRepositoryImpl
import ru.luckycactus.steamroulette.data.repositories.library.LibrarySettingsRepositoryImpl
import ru.luckycactus.steamroulette.data.repositories.login.LoginRepositoryImpl
import ru.luckycactus.steamroulette.data.repositories.login.datasource.LoginDataSource
import ru.luckycactus.steamroulette.data.repositories.login.datasource.RemoteLoginDataSource
import ru.luckycactus.steamroulette.data.repositories.review.AppReviewRepositoryImpl
import ru.luckycactus.steamroulette.data.repositories.user.UserRepositoryImpl
import ru.luckycactus.steamroulette.data.repositories.user.UserSessionRepositoryImpl
import ru.luckycactus.steamroulette.data.repositories.user.datasource.LocalUserDataSource
import ru.luckycactus.steamroulette.data.repositories.user.datasource.RemoteUserDataSource
import ru.luckycactus.steamroulette.data.repositories.user.datasource.UserDataSource
import ru.luckycactus.steamroulette.domain.about.AboutRepository
import ru.luckycactus.steamroulette.domain.app.AppRepository
import ru.luckycactus.steamroulette.domain.games.GameDetailsRepository
import ru.luckycactus.steamroulette.domain.games.GamesRepository
import ru.luckycactus.steamroulette.domain.games.RouletteRepository
import ru.luckycactus.steamroulette.domain.games_filter.LibraryFilterRepository
import ru.luckycactus.steamroulette.domain.games_filter.RouletteFilterRepository
import ru.luckycactus.steamroulette.domain.library.LibrarySettingsRepository
import ru.luckycactus.steamroulette.domain.login.LoginRepository
import ru.luckycactus.steamroulette.domain.review.AppReviewRepository
import ru.luckycactus.steamroulette.domain.user.UserRepository
import ru.luckycactus.steamroulette.domain.user.UserSessionRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindAppSettingsRepository(appRepositoryImpl: AppRepositoryImpl): AppRepository

    @Binds
    abstract fun bindUserRepository(userRepository: UserRepositoryImpl): UserRepository

    @Binds
    abstract fun bindUserSessionRepository(userSessionRepository: UserSessionRepositoryImpl): UserSessionRepository

    @Binds
    abstract fun bindLoginRepository(loginRepository: LoginRepositoryImpl): LoginRepository

    @Binds
    abstract fun bindLibraryFiltersRepository(repo: LibraryFilterRepositoryImpl): LibraryFilterRepository

    @Binds
    abstract fun bindRouletteFiltersRepository(repo: RouletteFilterRepositoryImpl): RouletteFilterRepository

    @Binds
    abstract fun bindGamesRepository(gamesRepository: GamesRepositoryImpl): GamesRepository

    @Binds
    abstract fun bindGameDetailsRepository(gameDetailsRepositoryImpl: GameDetailsRepositoryImpl): GameDetailsRepository

    @Binds
    abstract fun bindAboutRepository(aboutRepositoryImpl: AboutRepositoryImpl): AboutRepository

    @Binds
    abstract fun bindAppReviewRepository(appReviewRepositoryImpl: AppReviewRepositoryImpl): AppReviewRepository

    @Binds
    abstract fun bindLibrarySettingsRepository(librarySettingsRepositoryImpl: LibrarySettingsRepositoryImpl): LibrarySettingsRepository

    @Binds
    abstract fun bindRouletteRepository(rouletteRepositoryImpl: RouletteRepositoryImpl): RouletteRepository

    /** Data Sources */

    @Binds
    abstract fun bindRemoteUserDataSource(dataSource: RemoteUserDataSource): UserDataSource.Remote

    @Binds
    abstract fun bindRemoteGamesDataSource(remoteGamesDataSource: RemoteGamesDataSource): GamesDataSource.Remote

    @Binds
    abstract fun bindLocalGamesDataSource(localGamesDataSource: LocalGamesDataSource): GamesDataSource.Local

    @Binds
    abstract fun bindLocalUserDataSource(localUserDataSource: LocalUserDataSource): UserDataSource.Local

    @Binds
    abstract fun bindLoginDataSource(loginDataSource: RemoteLoginDataSource): LoginDataSource

    @Binds
    abstract fun bindAboutDataSource(localAboutDataSource: LocalAboutDataSource): AboutDataSource

    @Binds
    abstract fun bindRemoteGameDetailsDataSource(remoteGameDetailsDataSource: RemoteGameStoreDataSource): GameStoreDataSource.Remote
}