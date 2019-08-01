package ru.luckycactus.steamroulette.di

import android.app.Application
import android.content.Context
import android.util.LruCache
import androidx.room.Room
import com.google.gson.Gson
import ru.luckycactus.steamroulette.data.local.AndroidResourceManager
import ru.luckycactus.steamroulette.domain.games.GamesRepository
import ru.luckycactus.steamroulette.data.games.GamesRepositoryImpl
import ru.luckycactus.steamroulette.data.games.datastore.LocalGamesDataStore
import ru.luckycactus.steamroulette.data.games.datastore.RemoteGamesDataStore
import ru.luckycactus.steamroulette.data.games.mapper.OwnedGameMapper
import ru.luckycactus.steamroulette.data.local.CacheHelper
import ru.luckycactus.steamroulette.data.local.DB
import ru.luckycactus.steamroulette.data.local.PreferencesStorage
import ru.luckycactus.steamroulette.data.local.SharedPreferencesStorage
import ru.luckycactus.steamroulette.domain.login.LoginRepository
import ru.luckycactus.steamroulette.data.login.LoginRepositoryImpl
import ru.luckycactus.steamroulette.data.login.datastore.RemoteLoginDataStore
import ru.luckycactus.steamroulette.data.net.NetworkBoundResource
import ru.luckycactus.steamroulette.domain.user.UserRepository
import ru.luckycactus.steamroulette.data.user.UserRepositoryImpl
import ru.luckycactus.steamroulette.data.user.datastore.LocalUserDataStore
import ru.luckycactus.steamroulette.data.user.datastore.RemoteUserDataStore
import ru.luckycactus.steamroulette.data.user.datastore.UserDataStore
import ru.luckycactus.steamroulette.data.user.mapper.UserSummaryMapper
import ru.luckycactus.steamroulette.domain.common.ResourceManager
import ru.luckycactus.steamroulette.domain.games.GetOwnedGamesQueueUseCase
import ru.luckycactus.steamroulette.domain.games.GetOwnedGamesUseCase
import ru.luckycactus.steamroulette.domain.login.SignInUseCase
import ru.luckycactus.steamroulette.domain.login.ValidateSteamIdInputUseCase
import ru.luckycactus.steamroulette.domain.user.GetSignedInUserSteamIdUseCase
import ru.luckycactus.steamroulette.domain.user.GetUserSummaryUseCase
import ru.luckycactus.steamroulette.domain.login.SignOutUserUseCase

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

    val getSignedInUserSteamIdUseCase by lazy {
        GetSignedInUserSteamIdUseCase(
            userRepository
        )
    }

    val getOwnedGamesQueueUseCase by lazy {
        GetOwnedGamesQueueUseCase(
            GAMES_REPOSITORY
        )
    }

    val signOutUserUserCase by lazy {
        SignOutUserUseCase(userRepository)
    }

    val getOwnedGamesUseCase by lazy {
        GetOwnedGamesUseCase(GAMES_REPOSITORY)
    }

    val validateSteamIdInputUseCase by lazy {
        ValidateSteamIdInputUseCase()
    }

    val signInUseCase by lazy {
        SignInUseCase(
            getUserSummaryUseCase,
            userRepository,
            loginRepository
        )
    }

    val getUserSummaryUseCase by lazy {
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

    private val GAMES_REPOSITORY: GamesRepository by lazy {
        GamesRepositoryImpl(
            LOCAL_GAMES_DATA_STORE,
            REMOTE_GAMES_DATA_STORE,
            networkBoundResourceFactory
        )
    }

    private val LOCAL_GAMES_DATA_STORE: LocalGamesDataStore by lazy {
        LocalGamesDataStore(steamRouletteDb)
    }

    private val REMOTE_GAMES_DATA_STORE: RemoteGamesDataStore by lazy {
        RemoteGamesDataStore(NetworkModule.steamApiService)
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

    private val steamRouletteDb by lazy {
        Room.databaseBuilder(appContext, DB::class.java, "steam_roulette__db")
            .fallbackToDestructiveMigration() //todo remove
            .build()
    }
}