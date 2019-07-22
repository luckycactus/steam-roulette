package ru.luckycactus.steamroulette.di

import ru.luckycactus.steamroulette.data.games.SteamGamesRepository
import ru.luckycactus.steamroulette.data.games.SteamGamesRepositoryImpl
import ru.luckycactus.steamroulette.data.games.cache.SteamGamesCache
import ru.luckycactus.steamroulette.data.games.cache.SteamGamesCacheImpl
import ru.luckycactus.steamroulette.data.games.datastore.LocalSteamGamesDataStore
import ru.luckycactus.steamroulette.data.games.datastore.RemoteSteamGamesDataStore
import ru.luckycactus.steamroulette.data.games.datastore.SteamGamesDataStoreFactory
import ru.luckycactus.steamroulette.data.games.datastore.SteamGamesDataStoreFactoryImpl
import ru.luckycactus.steamroulette.data.games.mapper.OwnedGameMapper
import ru.luckycactus.steamroulette.data.local.SharedPreferencesStorage
import ru.luckycactus.steamroulette.data.login.LoginRepository
import ru.luckycactus.steamroulette.data.login.LoginRepositoryImpl
import ru.luckycactus.steamroulette.data.login.datastore.RemoteLoginDataStore
import ru.luckycactus.steamroulette.data.user.*
import ru.luckycactus.steamroulette.data.user.mapper.UserSummaryMapper
import ru.luckycactus.steamroulette.domain.*

object LoginScreenModule {


    val getOwnedGamesUseCase by lazy {
        GetOwnedGamesUseCase(steamGamesRepository)
    }

    val validateSteamIdInputUseCase by lazy {
        ValidateSteamIdInputUseCase()
    }

    val signInUseCase by lazy {
        SignInUseCase(
            resolveVanityUrlUseCase,
            getUserSummaryUseCase,
            userRepository
        )
    }

    private val resolveVanityUrlUseCase by lazy {
        ResolveVanityUrlUseCase(loginRepository)
    }

    private val getUserSummaryUseCase by lazy {
        GetUserSummaryUseCase(userRepository)
    }

    private val userRepository: UserRepository by lazy {
        UserRepositoryImpl(
            UserDataStoreFactoryImpl(
                userCache,
                remoteUserDataStore,
                localUserDataStore
            ),
            UserSummaryMapper(),
            AppModule.appPreferences
        )
    }

    private val remoteUserDataStore: UserDataStore by lazy {
        RemoteUserDataStore(
            NetworkModule.steamApiService,
            userCache
        )
    }

    private val localUserDataStore: UserDataStore by lazy {
        LocalUserDataStore(userCache)
    }

    private val userCache: UserCache by lazy {
        UserCacheImpl(
            SharedPreferencesStorage(AppModule.appContext, "user-cache"),
            AppModule.cacheHelper,
            AppModule.gson
        )
    }

    private val loginRepository: LoginRepository by lazy {
        LoginRepositoryImpl(remoteLoginDataStore)
    }

    private val remoteLoginDataStore: RemoteLoginDataStore by lazy {
        RemoteLoginDataStore(NetworkModule.steamApiService)
    }

    private val steamGamesRepository: SteamGamesRepository by lazy {
        SteamGamesRepositoryImpl(steamGamesDataStoreFactory, ownedGameMapper)
    }

    private val steamGamesDataStoreFactory: SteamGamesDataStoreFactory by lazy {
        SteamGamesDataStoreFactoryImpl(localSteamGamesDataStore, remoteSteamGamesDataStore, steamGamesCache)
    }

    private val localSteamGamesDataStore: LocalSteamGamesDataStore by lazy {
        LocalSteamGamesDataStore()
    }

    private val remoteSteamGamesDataStore: RemoteSteamGamesDataStore by lazy {
        RemoteSteamGamesDataStore(NetworkModule.steamApiService)
    }

    private val steamGamesCache: SteamGamesCache by lazy {
        SteamGamesCacheImpl()
    }

    private val ownedGameMapper by lazy {
        OwnedGameMapper()
    }

}