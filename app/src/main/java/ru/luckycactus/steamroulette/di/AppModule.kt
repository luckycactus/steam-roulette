package ru.luckycactus.steamroulette.di

import android.app.Application
import android.content.Context
import android.util.LruCache
import com.google.gson.Gson
import ru.luckycactus.steamroulette.data.AndroidResourceManager
import ru.luckycactus.steamroulette.data.games.SteamGamesRepository
import ru.luckycactus.steamroulette.data.games.SteamGamesRepositoryImpl
import ru.luckycactus.steamroulette.data.games.datastore.LocalSteamGamesDataStore
import ru.luckycactus.steamroulette.data.games.datastore.RemoteSteamGamesDataStore
import ru.luckycactus.steamroulette.data.games.mapper.OwnedGameMapper
import ru.luckycactus.steamroulette.data.local.CacheHelper
import ru.luckycactus.steamroulette.data.local.PreferencesStorage
import ru.luckycactus.steamroulette.data.local.SharedPreferencesStorage
import ru.luckycactus.steamroulette.data.login.LoginRepository
import ru.luckycactus.steamroulette.data.login.LoginRepositoryImpl
import ru.luckycactus.steamroulette.data.login.datastore.RemoteLoginDataStore
import ru.luckycactus.steamroulette.data.net.NetworkBoundResource
import ru.luckycactus.steamroulette.data.user.*
import ru.luckycactus.steamroulette.data.user.datastore.LocalUserDataStore
import ru.luckycactus.steamroulette.data.user.datastore.RemoteUserDataStore
import ru.luckycactus.steamroulette.data.user.datastore.UserDataStore
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
            localUserDataStore,
            remoteUserDataStore,
            UserSummaryMapper(),
            appPreferences,
            networkBoundResourceFactory
            )
    }

    private val remoteUserDataStore: UserDataStore.Remote by lazy {
        RemoteUserDataStore(
            NetworkModule.steamApiService
        )
    }

    private val localUserDataStore: UserDataStore.Local by lazy {
        LocalUserDataStore(
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
        SteamGamesRepositoryImpl(
            localSteamGamesDataStore,
            remoteSteamGamesDataStore,
            ownedGameMapper,
            networkBoundResourceFactory
        )
    }

    private val localSteamGamesDataStore: LocalSteamGamesDataStore by lazy {
        LocalSteamGamesDataStore()
    }

    private val remoteSteamGamesDataStore: RemoteSteamGamesDataStore by lazy {
        RemoteSteamGamesDataStore(NetworkModule.steamApiService)
    }

    private val ownedGameMapper by lazy {
        OwnedGameMapper()
    }

    private val networkBoundResourceFactory by lazy {
        NetworkBoundResource.Factory(
            cacheHelper,
            LruCache(50)
        )
    }

}