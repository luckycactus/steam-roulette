package ru.luckycactus.steamroulette.di

import android.app.Application
import android.content.Context
import com.google.gson.Gson
import ru.luckycactus.steamroulette.data.AndroidResourceManager
import ru.luckycactus.steamroulette.data.games.SteamGamesRepository
import ru.luckycactus.steamroulette.data.games.SteamGamesRepositoryImpl
import ru.luckycactus.steamroulette.data.games.cache.SteamGamesCache
import ru.luckycactus.steamroulette.data.games.cache.SteamGamesCacheImpl
import ru.luckycactus.steamroulette.data.games.datastore.LocalSteamGamesDataStore
import ru.luckycactus.steamroulette.data.games.datastore.RemoteSteamGamesDataStore
import ru.luckycactus.steamroulette.data.games.datastore.SteamGamesDataStoreFactory
import ru.luckycactus.steamroulette.data.games.datastore.SteamGamesDataStoreFactoryImpl
import ru.luckycactus.steamroulette.data.games.mapper.OwnedGameMapper
import ru.luckycactus.steamroulette.data.local.CacheHelper
import ru.luckycactus.steamroulette.data.local.PreferencesStorage
import ru.luckycactus.steamroulette.data.local.SharedPreferencesStorage
import ru.luckycactus.steamroulette.data.login.LoginRepository
import ru.luckycactus.steamroulette.data.login.LoginRepositoryImpl
import ru.luckycactus.steamroulette.data.login.datastore.RemoteLoginDataStore
import ru.luckycactus.steamroulette.data.user.*
import ru.luckycactus.steamroulette.data.user.mapper.UserSummaryMapper
import ru.luckycactus.steamroulette.domain.*
import ru.luckycactus.steamroulette.domain.common.ResourceManager
import ru.luckycactus.steamroulette.domain.user.GetSignedInUserUseCase
import ru.luckycactus.steamroulette.domain.user.IsUserSignedInUseCase
import ru.luckycactus.steamroulette.domain.user.SignOutUserUseCase

object AppModule {

    lateinit var appContext: Context

    lateinit var cacheHelper: CacheHelper
        private set

    lateinit var appPreferences: PreferencesStorage
        private set

    lateinit var resourceManager: ResourceManager
        private set

    val gson = Gson()

    fun init(app: Application) {
        appContext = app
        cacheHelper = CacheHelper(SharedPreferencesStorage(appContext, "cache-helper"))
        appPreferences = SharedPreferencesStorage(appContext, "app-prefs")
        resourceManager = AndroidResourceManager(appContext)
    }

    val isUserSignedInUseCase by lazy {
        IsUserSignedInUseCase(userRepository)
    }

    val getSignedInUserUseCase by lazy {
        GetSignedInUserUseCase(userRepository)
    }

    val signOutUserUserCase by lazy {
        SignOutUserUseCase(userRepository)
    }

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
            appPreferences
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
            SharedPreferencesStorage(appContext, "user-cache"),
            cacheHelper,
            gson
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